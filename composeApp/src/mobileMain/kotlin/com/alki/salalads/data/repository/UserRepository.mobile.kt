package com.alki.salalads.data.repository

import co.touchlab.kermit.Logger
import com.alki.salalads.data.models.CollectedQuestion
import com.alki.salalads.data.models.Friend
import com.alki.salalads.data.models.FriendAnswer
import com.alki.salalads.data.models.MyAnswer
import com.alki.salalads.data.models.ReceivedAnswer
import com.alki.salalads.data.models.SaladProgress
import com.alki.salalads.data.models.User
import com.alki.salalads.data.models.UserAttempt
import com.alki.salalads.data.models.UserIngredient
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Реализация UserRepository на основе Firebase Firestore (gitlive)
 * Для мобильных платформ (Android + iOS)
 */
@OptIn(ExperimentalTime::class)
actual class UserRepository actual constructor(
    private val authRepository: AuthRepository
) {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val usersCollection = firestore.collection("users")

    /**
     * Получить текущий ID пользователя
     */
    actual fun getCurrentUserId(): String =
        authRepository.currentUserId ?: throw IllegalStateException("User not authenticated")

    private fun currentUserId(): String = getCurrentUserId()

    private fun userDoc(userId: String = currentUserId()) = usersCollection.document(userId)

    // ============ USER PROFILE ============

    /**
     * Создать нового пользователя
     */
    actual suspend fun createUser(username: String, avatar: String): Result<User> {
        return try {
            val userId = currentUserId()
            val user = User(
                username = username,
                avatar = avatar,
                shareId = userId,
                lastIngredientAddedAt = 0,
                friendsCount = 0
            )
            userDoc().set(
                mapOf(
                    "username" to username,
                    "avatar" to avatar,
                    "shareId" to userId,
                    "lastIngredientAddedAt" to user.lastIngredientAddedAt,
                    "friendsCount" to 0
                )
            )
            Result.success(user)
        } catch (e: Exception) {
            Logger.e("Failed createUser()", e)
            Result.failure(e)
        }
    }

    /**
     * Получить текущего пользователя
     */
    actual suspend fun getCurrentUser(): User? {
        return try {
            val doc = userDoc().get()
            if (doc.exists) {
                User(
                    username = doc.get("username"),
                    avatar = doc.get("avatar"),
                    shareId = doc.get("shareId"),
                    lastIngredientAddedAt = doc.get("lastIngredientAddedAt"),
                    friendsCount = doc.get("friendsCount")
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getCurrentUser()", e)
            null
        }
    }

    /**
     * Проверить существует ли пользователь
     */
    actual suspend fun userExists(): Boolean {
        return try {
            userDoc().get().exists
        } catch (e: Exception) {
            Logger.e("Failed userExists()", e)
            false
        }
    }

    /**
     * Получить пользователя по shareId (для добавления в друзья)
     */
    actual suspend fun getUserByShareId(shareId: String): Pair<String, User>? {
        return try {
            val doc = usersCollection.document(shareId).get()
            if (doc.exists) {
                Pair(
                    doc.id,
                    User(
                        username = doc.get("username"),
                        avatar = doc.get("avatar"),
                        shareId = doc.get("shareId"),
                        lastIngredientAddedAt = doc.get("lastIngredientAddedAt"),
                        friendsCount = doc.get("friendsCount")
                    )
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getUserByShareId()", e)
            null
        }
    }

    // ============ INVENTORY (Мой Склад) ============

    /**
     * Добавить ингредиент в инвентарь
     */
    actual suspend fun addIngredientToInventory(ingredient: UserIngredient): Result<Unit> {
        return try {
            userDoc().collection("inventory").document(ingredient.ingredientId).set(
                mapOf(
                    "ingredientId" to ingredient.ingredientId,
                    "ru_name" to ingredient.ru_name,
                    "question" to ingredient.question,
                    "correctAnswer" to ingredient.correctAnswer,
                    "addedAt" to ingredient.addedAt
                )
            )
            // Обновляем время последнего добавления ингредиента
            userDoc().update(mapOf("lastIngredientAddedAt" to Clock.System.now().toEpochMilliseconds()))
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed addIngredientToInventory()", e)
            Result.failure(e)
        }
    }

    /**
     * Получить все ингредиенты текущего пользователя
     */
    actual suspend fun getInventory(): List<UserIngredient> {
        return getInventory(currentUserId())
    }

    /**
     * Получить все ингредиенты указанного пользователя
     */
    actual suspend fun getInventory(userId: String): List<UserIngredient> {
        return try {
            userDoc(userId).collection("inventory").get().documents.map { doc ->
                UserIngredient(
                    ingredientId = doc.get("ingredientId"),
                    ru_name = doc.get("ru_name"),
                    question = doc.get("question"),
                    correctAnswer = doc.get("correctAnswer"),
                    addedAt = doc.get("addedAt")
                )
            }
        } catch (e: Exception) {
            Logger.e("Failed getInventory()", e)
            emptyList()
        }
    }

    /**
     * Получить конкретный ингредиент друга
     */
    actual suspend fun getFriendIngredient(friendId: String, ingredientId: String): UserIngredient? {
        return try {
            val doc = userDoc(friendId).collection("inventory").document(ingredientId).get()
            if (doc.exists) {
                UserIngredient(
                    ingredientId = doc.get("ingredientId"),
                    ru_name = doc.get("ru_name"),
                    question = doc.get("question"),
                    correctAnswer = doc.get("correctAnswer"),
                    addedAt = doc.get("addedAt")
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getFriendIngredient()", e)
            null
        }
    }

    /**
     * Можно ли добавить новый ингредиент (прошло ли 1 час)
     */
    actual suspend fun canAddNewIngredient(): Boolean {
        val user = getCurrentUser() ?: return true
        val oneHourInMillis = 1 * 60 * 60 * 1000L
        val now = Clock.System.now().toEpochMilliseconds()
        return (now - user.lastIngredientAddedAt) >= oneHourInMillis
    }

    /**
     * Время до возможности добавить новый ингредиент (в миллисекундах)
     */
    actual suspend fun timeUntilCanAddIngredient(): Long {
        val user = getCurrentUser() ?: return 0
        val oneHourInMillis = 1 * 60 * 60 * 1000L
        val now = Clock.System.now().toEpochMilliseconds()
        val elapsed = now - user.lastIngredientAddedAt
        return maxOf(0, oneHourInMillis - elapsed)
    }

    // ============ RECEIVED ANSWERS ============

    /**
     * Сохранить ответ друга на мой вопрос
     */
    actual suspend fun saveReceivedAnswer(ingredientId: String, answer: ReceivedAnswer): Result<Unit> {
        return try {
            userDoc().collection("inventory")
                .document(ingredientId)
                .collection("received_answers")
                .add(
                    mapOf(
                        "answerText" to answer.answerText,
                        "isCorrect" to answer.isCorrect,
                        "timestamp" to answer.timestamp,
                        "friend_id" to answer.friend_id
                    )
                )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed saveReceivedAnswer()", e)
            Result.failure(e)
        }
    }

    /**
     * Сохранить ответ в коллекцию друга (когда я отвечаю на вопрос друга)
     */
    actual suspend fun saveAnswerToFriend(friendId: String, ingredientId: String, answer: ReceivedAnswer): Result<Unit> {
        return try {
            userDoc(friendId).collection("inventory")
                .document(ingredientId)
                .collection("received_answers")
                .add(
                    mapOf(
                        "answerText" to answer.answerText,
                        "isCorrect" to answer.isCorrect,
                        "timestamp" to answer.timestamp,
                        "friend_id" to answer.friend_id
                    )
                )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed saveAnswerToFriend()", e)
            Result.failure(e)
        }
    }

    /**
     * Сохранить ответ друга в подколлекцию friends_answers
     */
    actual suspend fun saveFriendAnswerAttempt(
        friendId: String,
        question: String,
        answer: String,
        fromUserName: String
    ): Result<Unit> {
        return try {
            userDoc(friendId).collection("friends_answers")
                .add(
                    mapOf(
                        "question" to question,
                        "answer" to answer,
                        "from_user" to fromUserName
                    )
                )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed saveFriendAnswerAttempt()", e)
            Result.failure(e)
        }
    }

    /**
     * Сохранить свой ответ в подколлекцию my_answers
     */
    actual suspend fun saveMyAnswer(
        userName: String,
        question: String,
        answer: String,
        isCorrect: Boolean
    ): Result<Unit> {
        return try {
            userDoc().collection("my_answers")
                .add(
                    mapOf(
                        "user_name" to userName,
                        "question" to question,
                        "answer" to answer,
                        "is_correct" to isCorrect
                    )
                )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed saveMyAnswer()", e)
            Result.failure(e)
        }
    }

    /**
     * Получить все ответы друзей (из подколлекции friends_answers)
     */
    actual suspend fun getFriendAnswers(): List<FriendAnswer> {
        return try {
            userDoc().collection("friends_answers")
                .get()
                .documents.map { doc ->
                    FriendAnswer(
                        question = doc.get("question"),
                        answer = doc.get("answer"),
                        from_user = doc.get("from_user")
                    )
                }
        } catch (e: Exception) {
            Logger.e("Failed getFriendAnswers()", e)
            emptyList()
        }
    }

    /**
     * Получить все мои ответы (из подколлекции my_answers)
     */
    actual suspend fun getMyAnswers(): List<MyAnswer> {
        return try {
            userDoc().collection("my_answers")
                .get()
                .documents.map { doc ->
                    MyAnswer(
                        user_name = doc.get("user_name"),
                        question = doc.get("question"),
                        answer = doc.get("answer")
                    )
                }
        } catch (e: Exception) {
            Logger.e("Failed getMyAnswers()", e)
            emptyList()
        }
    }

    /**
     * Получить все мои правильные ответы на вопросы друзей
     */
    actual suspend fun getMyCorrectAnswers(): List<MyAnswer> {
        return try {
            userDoc()
                .collection("my_answers").get()
                .documents
                .mapNotNull { doc ->
                    val isCorrect = doc.get<Boolean>("is_correct")
                    if (isCorrect) {
                        MyAnswer(
                            user_name = doc.get("user_name"),
                            question = doc.get("question"),
                            answer = doc.get("answer")
                        )
                    } else null
                }
        } catch (e: Exception) {
            Logger.e("Failed getMyCorrectAnswers()", e)
            emptyList()
        }
    }

    /**
     * Получить все ответы на вопрос по ингредиенту
     */
    actual suspend fun getReceivedAnswers(ingredientId: String): List<ReceivedAnswer> {
        return try {
            userDoc().collection("inventory")
                .document(ingredientId)
                .collection("received_answers")
                .get()
                .documents.map { doc ->
                    ReceivedAnswer(
                        answerText = doc.get("answerText"),
                        isCorrect = doc.get("isCorrect"),
                        timestamp = doc.get("timestamp"),
                        friend_id = doc.get("friend_id")
                    )
                }
        } catch (e: Exception) {
            Logger.e("Failed getReceivedAnswers()", e)
            emptyList()
        }
    }

    // ============ FRIENDS ============

    /**
     * Добавить друга
     */
    actual suspend fun addFriend(friendId: String, friend: Friend): Result<Unit> {
        return try {
            // Добавляем друга к себе
            userDoc().collection("friends").document(friendId).set(
                mapOf(
                    "username" to friend.username,
                    "avatar" to friend.avatar,
                    "addedAt" to friend.addedAt
                )
            )
            // Увеличиваем счетчик друзей
            userDoc().update(mapOf("friendsCount" to FieldValue.increment(1)))

            // Добавляем себя к другу (взаимная дружба)
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                userDoc(friendId).collection("friends").document(currentUserId()).set(
                    mapOf(
                        "username" to currentUser.username,
                        "avatar" to currentUser.avatar,
                        "addedAt" to Clock.System.now().toEpochMilliseconds()
                    )
                )
                userDoc(friendId).update(mapOf("friendsCount" to FieldValue.increment(1)))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed addFriend()", e)
            Result.failure(e)
        }
    }

    /**
     * Получить список друзей
     */
    actual suspend fun getFriends(): List<Pair<String, Friend>> {
        return try {
            userDoc().collection("friends").get().documents.map { doc ->
                Pair(
                    doc.id,
                    Friend(
                        username = doc.get("username"),
                        avatar = doc.get("avatar"),
                        addedAt = doc.get("addedAt")
                    )
                )
            }
        } catch (e: Exception) {
            Logger.e("Failed getFriends()", e)
            emptyList()
        }
    }

    /**
     * Проверить, является ли пользователь другом
     */
    actual suspend fun isFriend(userId: String): Boolean {
        return try {
            userDoc().collection("friends").document(userId).get().exists
        } catch (e: Exception) {
            Logger.e("Failed isFriend()", e)
            false
        }
    }

    /**
     * Удалить друга (взаимное удаление)
     */
    actual suspend fun deleteFriend(friendId: String): Result<Unit> {
        return try {
            // Удаляем друга у себя
            userDoc().collection("friends").document(friendId).delete()
            // Уменьшаем счетчик друзей
            userDoc().update(mapOf("friendsCount" to FieldValue.increment(-1)))

            // Удаляем себя у друга (взаимное удаление)
            userDoc(friendId).collection("friends").document(currentUserId()).delete()
            userDoc(friendId).update(mapOf("friendsCount" to FieldValue.increment(-1)))

            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed deleteFriend()", e)
            Result.failure(e)
        }
    }

    // ============ SALADS PROGRESS ============

    /**
     * Добавить салат в прогресс
     */
    actual suspend fun addSaladToProgress(saladId: String, totalIngredients: Int): Result<Unit> {
        return try {
            userDoc().collection("salads_progress").document(saladId).set(
                mapOf(
                    "saladId" to saladId,
                    "isCompleted" to false,
                    "collectedIngredients" to emptyMap<String, Boolean>(),
                    "collectedCount" to 0,
                    "collectedQuestions" to emptyList<Map<String, String>>()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed addSaladToProgress()", e)
            Result.failure(e)
        }
    }

    /**
     * Получить прогресс всех салатов
     */
    actual suspend fun getSaladsProgress(): List<SaladProgress> {
        return try {
            userDoc().collection("salads_progress").get().documents.map { doc ->
                val questionsData = doc.get<List<Map<String, String>>?>("collectedQuestions") ?: emptyList()
                val questions = questionsData.map { map ->
                    CollectedQuestion(
                        friend_username = map["friend_username"] ?: "",
                        question = map["question"] ?: "",
                        correctAnswer = map["correctAnswer"] ?: ""
                    )
                }
                SaladProgress(
                    saladId = doc.get("saladId"),
                    isCompleted = doc.get("isCompleted"),
                    collectedIngredients = doc.get("collectedIngredients"),
                    collectedCount = doc.get("collectedCount"),
                    collectedQuestions = questions
                )
            }
        } catch (e: Exception) {
            Logger.e("Failed getSaladsProgress()", e)
            emptyList()
        }
    }

    /**
     * Получить прогресс конкретного салата
     */
    actual suspend fun getSaladProgress(saladId: String): SaladProgress? {
        return try {
            val doc = userDoc().collection("salads_progress").document(saladId).get()
            if (doc.exists) {
                val questionsData = doc.get<List<Map<String, String>>?>("collectedQuestions") ?: emptyList()
                val questions = questionsData.map { map ->
                    CollectedQuestion(
                        friend_username = map["friend_username"] ?: "",
                        question = map["question"] ?: "",
                        correctAnswer = map["correctAnswer"] ?: ""
                    )
                }
                SaladProgress(
                    saladId = doc.get("saladId"),
                    isCompleted = doc.get("isCompleted"),
                    collectedIngredients = doc.get("collectedIngredients"),
                    collectedCount = doc.get("collectedCount"),
                    collectedQuestions = questions
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getSaladProgress()", e)
            null
        }
    }

    /**
     * Добавить собранный ингредиент к салату (без вопроса)
     */
    actual suspend fun addCollectedIngredient(
        saladId: String,
        ingredientId: String
    ): Result<Unit> {
        return addCollectedIngredientInternal(saladId, ingredientId, null)
    }

    /**
     * Добавить собранный ингредиент к салату с вопросом
     */
    actual suspend fun addCollectedIngredientWithQuestion(
        saladId: String,
        ingredientId: String,
        collectedQuestion: CollectedQuestion
    ): Result<Unit> {
        return addCollectedIngredientInternal(saladId, ingredientId, collectedQuestion)
    }

    /**
     * Внутренняя реализация добавления ингредиента к салату
     */
    private suspend fun addCollectedIngredientInternal(
        saladId: String,
        ingredientId: String,
        collectedQuestion: CollectedQuestion?
    ): Result<Unit> {
        return try {
            val progress = getSaladProgress(saladId) ?: return Result.failure(Exception("Salad not found"))
            val newCollected = progress.collectedIngredients.toMutableMap()
            newCollected[ingredientId] = true
            val newCount = newCollected.size

            val updateMap = mutableMapOf<String, Any>(
                "collectedIngredients" to newCollected,
                "collectedCount" to newCount
            )

            // Если передан вопрос от друга, добавляем его в массив
            if (collectedQuestion != null) {
                val newQuestions = progress.collectedQuestions.toMutableList()
                newQuestions.add(collectedQuestion)

                // Преобразуем в список Map для Firebase
                val questionsForFirebase = newQuestions.map { q ->
                    mapOf(
                        "friend_username" to q.friend_username,
                        "question" to q.question,
                        "correctAnswer" to q.correctAnswer
                    )
                }
                updateMap["collectedQuestions"] = questionsForFirebase
            }

            userDoc().collection("salads_progress").document(saladId).update(updateMap)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed addCollectedIngredient()", e)
            Result.failure(e)
        }
    }

    /**
     * Отметить салат как завершенный
     */
    actual suspend fun completeSalad(saladId: String): Result<Unit> {
        return try {
            userDoc().collection("salads_progress").document(saladId).update(
                mapOf("isCompleted" to true)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed completeSalad()", e)
            Result.failure(e)
        }
    }

    // ============ ATTEMPTS ============

    /**
     * Получить попытку ответа
     */
    actual suspend fun getAttempt(friendId: String, ingredientId: String): UserAttempt? {
        return try {
            val attemptId = "${friendId}_${ingredientId}"
            val doc = userDoc().collection("attempts").document(attemptId).get()
            if (doc.exists) {
                UserAttempt(
                    attemptsCount = doc.get("attemptsCount"),
                    lastAttemptAt = doc.get("lastAttemptAt"),
                    blockedUntil = doc.get<Long?>("blockedUntil")
                )
            } else null
        } catch (e: Exception) {
            Logger.e("Failed getAttempt()", e)
            null
        }
    }

    /**
     * Сохранить неудачную попытку
     */
    actual suspend fun saveFailedAttempt(friendId: String, ingredientId: String): Result<UserAttempt> {
        return try {
            val attemptId = "${friendId}_${ingredientId}"
            val existing = getAttempt(friendId, ingredientId)
            val now = Clock.System.now().toEpochMilliseconds()

            val newAttempt = if (existing == null) {
                // Первая неудачная попытка
                UserAttempt(attemptsCount = 1, lastAttemptAt = now, blockedUntil = null)
            } else if (existing.attemptsCount == 1) {
                // Вторая неудачная попытка - блокируем на 30 минут
                val thirtyMinutesInMillis = 30 * 60 * 1000L
                UserAttempt(attemptsCount = 2, lastAttemptAt = now, blockedUntil = now + thirtyMinutesInMillis)
            } else {
                // Уже было 2 попытки, сбрасываем и снова блокируем
                val thirtyMinutesInMillis = 30 * 60 * 1000L
                UserAttempt(attemptsCount = 2, lastAttemptAt = now, blockedUntil = now + thirtyMinutesInMillis)
            }

            userDoc().collection("attempts").document(attemptId).set(
                mapOf(
                    "attemptsCount" to newAttempt.attemptsCount,
                    "lastAttemptAt" to newAttempt.lastAttemptAt,
                    "blockedUntil" to newAttempt.blockedUntil
                )
            )

            Result.success(newAttempt)
        } catch (e: Exception) {
            Logger.e("Failed saveFailedAttempt()", e)
            Result.failure(e)
        }
    }

    /**
     * Сбросить попытки после успешного ответа
     */
    actual suspend fun clearAttempt(friendId: String, ingredientId: String): Result<Unit> {
        return try {
            val attemptId = "${friendId}_${ingredientId}"
            userDoc().collection("attempts").document(attemptId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed clearAttempt()", e)
            Result.failure(e)
        }
    }

    /**
     * Проверить заблокирована ли попытка
     */
    actual suspend fun isAttemptBlocked(friendId: String, ingredientId: String): Boolean {
        val attempt = getAttempt(friendId, ingredientId) ?: return false
        val blockedUntil = attempt.blockedUntil ?: return false
        return Clock.System.now().toEpochMilliseconds() < blockedUntil
    }

    /**
     * Время до разблокировки (в миллисекундах)
     */
    actual suspend fun getTimeUntilUnblock(friendId: String, ingredientId: String): Long {
        val attempt = getAttempt(friendId, ingredientId) ?: return 0
        val blockedUntil = attempt.blockedUntil ?: return 0
        val now = Clock.System.now().toEpochMilliseconds()
        return maxOf(0, blockedUntil - now)
    }

    // ============ FRIENDS WITH INGREDIENT ============

    /**
     * Найти друзей, у которых есть определенный ингредиент
     */
    actual suspend fun getFriendsWithIngredient(ingredientId: String): List<Pair<String, Friend>> {
        val friends = getFriends()
        val result = mutableListOf<Pair<String, Friend>>()

        for ((friendId, friend) in friends) {
            val inventory = getInventory(friendId)
            if (inventory.any { it.ingredientId == ingredientId }) {
                result.add(Pair(friendId, friend))
            }
        }

        return result
    }

    /**
     * Обновить вопрос и ответ для ингредиента
     */
    actual suspend fun updateIngredient(ingredientId: String, question: String, correctAnswer: String): Result<Unit> {
        return try {
            userDoc().collection("inventory").document(ingredientId).update(
                mapOf(
                    "question" to question,
                    "correctAnswer" to correctAnswer
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("Failed updateIngredient()", e)
            Result.failure(e)
        }
    }

    /**
     * Поток инвентаря
     */
    actual fun observeInventory(): Flow<List<UserIngredient>> = flow {
        userDoc().collection("inventory").snapshots.collect { snapshot ->
            val inventory = snapshot.documents.map { doc ->
                UserIngredient(
                    ingredientId = doc.get("ingredientId"),
                    ru_name = doc.get("ru_name"),
                    question = doc.get("question"),
                    correctAnswer = doc.get("correctAnswer"),
                    addedAt = doc.get("addedAt")
                )
            }
            emit(inventory)
        }
    }

    /**
     * Поток прогресса салатов
     */
    actual fun observeSaladsProgress(): Flow<List<SaladProgress>> = flow {
        userDoc().collection("salads_progress").snapshots.collect { snapshot ->
            val progress = snapshot.documents.map { doc ->
                val questionsData = doc.get<List<Map<String, String>>?>("collectedQuestions") ?: emptyList()
                val questions = questionsData.map { map ->
                    CollectedQuestion(
                        friend_username = map["friend_username"] ?: "",
                        question = map["question"] ?: "",
                        correctAnswer = map["correctAnswer"] ?: ""
                    )
                }
                SaladProgress(
                    saladId = doc.get("saladId"),
                    isCompleted = doc.get("isCompleted"),
                    collectedIngredients = doc.get("collectedIngredients"),
                    collectedCount = doc.get("collectedCount"),
                    collectedQuestions = questions
                )
            }
            emit(progress)
        }
    }

    /**
     * Поток друзей
     */
    actual fun observeFriends(): Flow<List<Pair<String, Friend>>> = flow {
        userDoc().collection("friends").snapshots.collect { snapshot ->
            val friends = snapshot.documents.map { doc ->
                Pair(
                    doc.id,
                    Friend(
                        username = doc.get("username"),
                        avatar = doc.get("avatar"),
                        addedAt = doc.get("addedAt")
                    )
                )
            }
            emit(friends)
        }
    }
}

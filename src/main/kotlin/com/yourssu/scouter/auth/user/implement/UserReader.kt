package com.yourssu.scouter.auth.user.implement

import com.yourssu.scouter.auth.authentication.implement.OAuth2User
import com.yourssu.scouter.auth.support.implement.exception.UserNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class UserReader(
    private val userRepository: UserRepository,
) {

    fun existsById(userId: Long): Boolean {
        return userRepository.existsById(userId)
    }

    fun find(oauth2User: OAuth2User): User? {
        val oauthId: String = oauth2User.userInfo.oauthId

        return userRepository.findByOAuthId(oauthId)
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun readById(userId: Long): User {
        return userRepository.findById(userId) ?: throw UserNotFoundException("지정한 사용자를 찾을 수 없습니다.")
    }

    fun readAllByIds(userIds: Collection<Long>): List<User> {
        return userRepository.findAllByIds(userIds)
    }

    fun readAllByEmails(emails: Collection<String>): List<User> {
        return userRepository.findAllByEmails(emails)
    }
}

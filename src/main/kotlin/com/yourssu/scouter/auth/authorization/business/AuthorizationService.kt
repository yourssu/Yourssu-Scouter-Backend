package com.yourssu.scouter.auth.authorization.business

import com.yourssu.scouter.auth.authorization.business.dto.RoleHolderDto
import com.yourssu.scouter.auth.authorization.implement.Role
import com.yourssu.scouter.auth.authorization.implement.UserRoleReader
import com.yourssu.scouter.auth.authorization.implement.UserRoleWriter
import com.yourssu.scouter.auth.support.exception.LastRoleHolderException
import com.yourssu.scouter.auth.support.exception.UserNotFoundException
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthorizationService(
    private val userRoleReader: UserRoleReader,
    private val userRoleWriter: UserRoleWriter,
    private val userReader: UserReader,
) {

    fun hasRole(userId: Long, role: Role): Boolean {
        return userRoleReader.hasRole(userId, role)
    }

    fun readHolders(role: Role): List<RoleHolderDto> {
        val userIds: List<Long> = userRoleReader.readAllByRole(role).map { it.userId }
        if (userIds.isEmpty()) {
            return emptyList()
        }

        return userReader.readAllByIds(userIds)
            .map { RoleHolderDto(userId = it.id!!, name = it.userInfo.name, email = it.userInfo.email) }
            .sortedBy { it.email }
    }

    /**
     * 이미 가진 사용자에게는 아무것도 하지 않고 false 를 돌려준다.
     * user_role 에는 (user_id, role) 유니크 제약이 있어(V25) 그냥 넣으면 제약 위반으로 터진다.
     *
     * @return 이번 호출로 새로 부여되었으면 true
     */
    @Transactional
    fun grant(email: String, role: Role): Boolean {
        val user: User = readUserByEmail(email)
        if (userRoleReader.hasRole(user.id!!, role)) {
            return false
        }

        userRoleWriter.write(user.id!!, role)
        log.info("[권한 부여] role={} userId={} email={}", role, user.id, email)

        return true
    }

    /**
     * 마지막 한 명은 회수하지 않는다. 이 권한은 internal dev API 호출 자격과 개인정보 마스킹 해제를 결정하는데,
     * 0명이 되면 코드 상으로 다시 부여할 수 있는 사람이 아무도 남지 않는다. 되돌리려면 DB 를 직접 건드려야 한다.
     *
     * @return 이번 호출로 회수되었으면 true. 애초에 없었으면 false
     */
    @Transactional
    fun revoke(email: String, role: Role): Boolean {
        val user: User = readUserByEmail(email)
        if (!userRoleReader.hasRole(user.id!!, role)) {
            return false
        }
        if (userRoleReader.countByRole(role) <= 1) {
            throw LastRoleHolderException("마지막 $role 권한은 회수할 수 없습니다. 먼저 다른 사용자에게 부여하세요.")
        }

        userRoleWriter.delete(user.id!!, role)
        log.info("[권한 회수] role={} userId={} email={}", role, user.id, email)

        return true
    }

    private fun readUserByEmail(email: String): User {
        return userReader.findByEmail(email)
            ?: throw UserNotFoundException("해당 이메일로 가입한 사용자가 없습니다: $email")
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthorizationService::class.java)
    }
}

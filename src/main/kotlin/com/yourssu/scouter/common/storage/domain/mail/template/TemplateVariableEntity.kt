package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import jakarta.persistence.*

@Entity
@Table(name = "mail_template_variable")
class TemplateVariableEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    val template: MailTemplateEntity,

    @Column(nullable = false)
    val variableKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val variableType: VariableType,

    @Column(nullable = false)
    val displayName: String,

    @Column(nullable = false)
    val perRecipient: Boolean,

    @Column(nullable = false)
    val requiresUserInput: Boolean,
)

fun TemplateVariableEntity.toDomain(): TemplateVariable = TemplateVariable(
    key = variableKey,
    type = variableType,
    displayName = displayName,
    perRecipient = perRecipient,
    requiresUserInput = requiresUserInput,
)

object TemplateVariableEntityFactory {
    fun fromList(variables: List<TemplateVariable>, template: MailTemplateEntity): List<TemplateVariableEntity> {
        return variables.map {
            TemplateVariableEntity(
                template = template,
                variableKey = it.key,
                variableType = it.type,
                displayName = it.displayName,
                perRecipient = it.perRecipient,
                requiresUserInput = it.requiresUserInput,
            )
        }
    }
}

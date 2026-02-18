package com.yourssu.scouter.common.storage.domain.mail.template

import com.yourssu.scouter.common.implement.domain.mail.template.TemplateVariable
import com.yourssu.scouter.common.implement.domain.mail.template.VariableType
import jakarta.persistence.*

@Entity
@Table(
    name = "mail_template_variable",
    uniqueConstraints = [UniqueConstraint(
        name = "uk_template_variable",
        columnNames = ["template_id", "variable_key"]
    )]
)
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
) {
    fun toDomain(): TemplateVariable = TemplateVariable(
        key = variableKey,
        type = variableType,
        displayName = displayName,
        perRecipient = perRecipient,
    )

    companion object {
        fun from(
            variable: TemplateVariable,
            template: MailTemplateEntity,
        ): TemplateVariableEntity {
            return TemplateVariableEntity(
                template = template,
                variableKey = variable.key,
                variableType = variable.type,
                displayName = variable.displayName,
                perRecipient = variable.perRecipient,
            )
        }

        fun fromList(
            variables: List<TemplateVariable>,
            template: MailTemplateEntity,
        ): List<TemplateVariableEntity> {
            return variables.map { from(it, template) }
        }
    }
}

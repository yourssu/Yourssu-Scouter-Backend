package com.yourssu.scouter.mail.template.storage

import com.yourssu.scouter.mail.template.implement.TemplateVariable
import com.yourssu.scouter.mail.template.implement.VariableType
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

    @Column
    val attributeKey: String? = null,
) {
    fun toDomain(): TemplateVariable = when {
        variableType.isUserInputType() -> TemplateVariable.UserInput(
            key = variableKey,
            displayName = displayName,
            type = variableType,
            perRecipient = perRecipient,
        )
        variableType == VariableType.APPLICANT -> TemplateVariable.ApplicantBound(
            key = variableKey,
            displayName = displayName,
            attributeKey = requireNotNull(attributeKey) { "APPLICANT 변수에 attributeKey가 없습니다: $variableKey" },
        )
        variableType == VariableType.PARTNAME -> TemplateVariable.PartName(
            key = variableKey,
            displayName = displayName,
        )
        else -> throw IllegalStateException("알 수 없는 variableType: $variableType")
    }

    companion object {
        fun from(
            variable: TemplateVariable,
            template: MailTemplateEntity,
        ): TemplateVariableEntity = when (variable) {
            is TemplateVariable.UserInput -> TemplateVariableEntity(
                template = template,
                variableKey = variable.key,
                variableType = variable.type,
                displayName = variable.displayName,
                perRecipient = variable.perRecipient,
                attributeKey = null,
            )
            is TemplateVariable.ApplicantBound -> TemplateVariableEntity(
                template = template,
                variableKey = variable.key,
                variableType = VariableType.APPLICANT,
                displayName = variable.displayName,
                perRecipient = false,
                attributeKey = variable.attributeKey,
            )
            is TemplateVariable.PartName -> TemplateVariableEntity(
                template = template,
                variableKey = variable.key,
                variableType = VariableType.PARTNAME,
                displayName = variable.displayName,
                perRecipient = false,
                attributeKey = null,
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

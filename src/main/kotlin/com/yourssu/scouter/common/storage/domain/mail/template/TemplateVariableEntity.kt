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
)

fun TemplateVariableEntity.toDomain(): TemplateVariable = TemplateVariable(
    key = variableKey,
    type = variableType,
    displayName = displayName,
    perRecipient = perRecipient,
)

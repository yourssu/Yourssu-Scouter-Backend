package com.yourssu.scouter.common.storage.domain.mail.template

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mail_template")
class MailTemplateEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val title: String,

    @Lob
    @Column(nullable = false)
    val bodyHtml: String,

    @Column(nullable = false)
    val createdBy: Long,

    @Column(nullable = false)
    val createdAt: LocalDateTime,

    @Column(nullable = false)
    val updatedAt: LocalDateTime,

    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    val variables: MutableList<TemplateVariableEntity> = mutableListOf(),
)

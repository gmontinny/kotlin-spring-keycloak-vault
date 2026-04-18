package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "order_reviews")
class OrderReview(
    @Id
    @Column(name = "review_id", length = 50)
    val reviewId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    val order: Order,

    @Column(name = "review_score", nullable = false)
    val reviewScore: Int,

    @Column(name = "review_comment_title")
    val reviewCommentTitle: String? = null,

    @Column(name = "review_comment_message", columnDefinition = "TEXT")
    val reviewCommentMessage: String? = null,

    @Column(name = "review_creation_date", nullable = false)
    val reviewCreationDate: LocalDateTime,

    @Column(name = "review_answer_timestamp", nullable = false)
    val reviewAnswerTimestamp: LocalDateTime
)

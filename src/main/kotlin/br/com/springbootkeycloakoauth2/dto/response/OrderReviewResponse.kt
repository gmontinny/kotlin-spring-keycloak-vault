package br.com.springbootkeycloakoauth2.dto.response

import org.springframework.hateoas.RepresentationModel
import java.time.LocalDateTime

data class OrderReviewResponse(
    val reviewId: String,
    val orderId: String,
    val reviewScore: Int,
    val reviewCommentTitle: String?,
    val reviewCommentMessage: String?,
    val reviewCreationDate: LocalDateTime,
    val reviewAnswerTimestamp: LocalDateTime
) : RepresentationModel<OrderReviewResponse>()

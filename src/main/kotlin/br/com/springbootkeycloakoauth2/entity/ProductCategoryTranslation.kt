package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.*

@Entity
@Table(name = "product_category_translations")
class ProductCategoryTranslation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_category_name", length = 100, nullable = false, unique = true)
    val productCategoryName: String,

    @Column(name = "product_category_name_english", length = 100, nullable = false)
    val productCategoryNameEnglish: String
)

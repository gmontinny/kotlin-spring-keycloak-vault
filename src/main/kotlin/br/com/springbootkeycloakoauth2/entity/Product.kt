package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "products")
class Product(
    @Id
    @Column(name = "product_id", length = 50)
    val productId: String,

    @Column(name = "product_category_name", length = 100)
    val productCategoryName: String? = null,

    @Column(name = "product_name_length")
    val productNameLength: Int? = null,

    @Column(name = "product_description_length")
    val productDescriptionLength: Int? = null,

    @Column(name = "product_photos_qty")
    val productPhotosQty: Int? = null,

    @Column(name = "product_weight_g")
    val productWeightG: Int? = null,

    @Column(name = "product_length_cm")
    val productLengthCm: Int? = null,

    @Column(name = "product_height_cm")
    val productHeightCm: Int? = null,

    @Column(name = "product_width_cm")
    val productWidthCm: Int? = null
)

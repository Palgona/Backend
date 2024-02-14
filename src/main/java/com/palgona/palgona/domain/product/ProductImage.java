package com.palgona.palgona.domain.product;

import com.palgona.palgona.domain.image.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_image", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "image_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_image_to_product"))
    private Product product;

    @OneToOne
    @JoinColumn(name = "image_id", foreignKey = @ForeignKey(name = "fk_product_image_to_image"))
    private Image image;

    @Builder
    public ProductImage(Product product, Image image){
        this.product = product;
        this.image = image;
    }
}
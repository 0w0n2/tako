package com.bukadong.tcg.api.admin.card.dto.response;

import com.bukadong.tcg.api.card.entity.Attribute;
import com.bukadong.tcg.api.card.entity.Card;
import com.bukadong.tcg.api.card.entity.Rarity;
import com.bukadong.tcg.api.category.entity.CategoryMajor;
import com.bukadong.tcg.api.category.entity.CategoryMedium;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateCardRequestDto(
        @NotNull(message = "카테고리 대분류 ID를 입력해주세요.")
        Long categoryMajorId,

        @NotNull(message = "카테고리 중분류 ID를 입력해주세요.")
        Long categoryMediumId,

        @Size(max = 30, message = "카드 이름은 30자 이내여야 합니다.")
        @NotBlank(message = "카드 이름을 입력해주세요.")
        String name,

        @Size(max = 30, message = "카드 코드는 30자 이내여야 합니다.")
        String code,

        @NotBlank(message = "카드 설명을 입력해주세요.")
        String description,

        String attribute,

        String rarity
) {
    public Card toCard(CategoryMajor categoryMajor, CategoryMedium categoryMedium) {
        return Card.builder()
                .categoryMajor(categoryMajor)
                .categoryMedium(categoryMedium)
                .code(this.code)
                .name(this.name)
                .description(this.description)
                .rarity(Rarity.getRarity(this.rarity))
                .attribute(Attribute.getAttribute(this.attribute))
                .build();
    }
}

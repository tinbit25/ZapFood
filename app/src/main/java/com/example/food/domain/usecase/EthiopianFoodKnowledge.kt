package com.example.food.domain.usecase

import com.example.food.data.model.*

data class MealMetadataSuggestion(
    val category: EthiopianFoodCategory,
    val fastingFriendly: Boolean,
    val veganFriendly: Boolean,
    val proteinLevel: ProteinLevel,
    val spiceLevel: SpiceLevel,
    val carbLevel: CarbLevel,
    val oilLevel: OilLevel,
    val tags: List<String>,
    val ingredients: List<String>,
    val mealRegion: String
)

object EthiopianFoodKnowledge {

    val STANDARD_TAGS = listOf(
        "spicy", "vegetarian", "habesha", "cultural", "family-size",
        "budget-friendly", "breakfast", "protein-rich", "healthy", "traditional",
        "fasting", "meat"
    )

    fun suggestMetadataForMeal(mealName: String): MealMetadataSuggestion {
        val lowerName = mealName.lowercase()

        // Default assumptions
        var category = EthiopianFoodCategory.GENERAL
        var fastingFriendly = false
        var veganFriendly = false
        var proteinLevel = ProteinLevel.MEDIUM
        var spiceLevel = SpiceLevel.MEDIUM
        var carbLevel = CarbLevel.MEDIUM
        var oilLevel = OilLevel.MEDIUM
        val tags = mutableSetOf<String>("habesha", "cultural")
        var ingredients = emptyList<String>()
        var mealRegion = "Ethiopia"

        // Knowledge Base Rules
        when {
            lowerName.contains("shiro") -> {
                category = EthiopianFoodCategory.FASTING_FOODS
                fastingFriendly = true
                veganFriendly = true
                proteinLevel = ProteinLevel.MEDIUM
                oilLevel = OilLevel.MEDIUM
                tags.addAll(listOf("fasting", "vegetarian", "traditional"))
                ingredients = listOf("shiro powder", "berbere", "onion", "garlic", "oil")
                mealRegion = "All Regions"
            }
            lowerName.contains("kitfo") -> {
                category = EthiopianFoodCategory.MEAT_FOODS
                proteinLevel = ProteinLevel.HIGH
                spiceLevel = SpiceLevel.VERY_SPICY
                oilLevel = OilLevel.HIGH
                tags.addAll(listOf("meat", "protein-rich", "traditional", "spicy"))
                ingredients = listOf("minced beef", "mitmita", "niter kibbeh")
                mealRegion = "Gurage"
            }
            lowerName.contains("doro") || lowerName.contains("wat") -> {
                category = if (lowerName.contains("doro")) EthiopianFoodCategory.MEAT_FOODS else EthiopianFoodCategory.TRADITIONAL
                proteinLevel = ProteinLevel.HIGH
                spiceLevel = SpiceLevel.SPICY
                tags.addAll(listOf("traditional", "spicy"))
                if (lowerName.contains("doro")) {
                    tags.add("meat")
                    ingredients = listOf("chicken", "berbere", "onion", "niter kibbeh", "boiled eggs")
                    mealRegion = "Amhara"
                }
            }
            lowerName.contains("firfir") || lowerName.contains("chechebsa") || lowerName.contains("kinche") -> {
                category = EthiopianFoodCategory.BREAKFAST
                carbLevel = CarbLevel.HIGH
                tags.addAll(listOf("breakfast", "traditional"))
                if (lowerName.contains("firfir")) ingredients = listOf("injera", "berbere", "onion")
            }
            lowerName.contains("tibs") -> {
                category = EthiopianFoodCategory.MEAT_FOODS
                proteinLevel = ProteinLevel.HIGH
                tags.addAll(listOf("meat", "protein-rich"))
            }
            lowerName.contains("misir") || lowerName.contains("kik") -> {
                category = EthiopianFoodCategory.FASTING_FOODS
                fastingFriendly = true
                veganFriendly = true
                proteinLevel = ProteinLevel.HIGH
                tags.addAll(listOf("fasting", "vegetarian", "protein-rich"))
            }
            lowerName.contains("gomen") || lowerName.contains("dinich") || lowerName.contains("salata") -> {
                category = EthiopianFoodCategory.HEALTHY
                fastingFriendly = true
                veganFriendly = true
                proteinLevel = ProteinLevel.LOW
                tags.addAll(listOf("healthy", "vegetarian", "fasting"))
            }
            lowerName.contains("beyaynetu") || lowerName.contains("mahaberawi") -> {
                category = EthiopianFoodCategory.FAMILY_MEALS
                if (lowerName.contains("beyaynetu")) {
                    fastingFriendly = true
                    veganFriendly = true
                    tags.addAll(listOf("fasting", "vegetarian", "family-size"))
                } else {
                    proteinLevel = ProteinLevel.HIGH
                    tags.addAll(listOf("meat", "family-size"))
                }
            }
        }

        // Catch-all tag assignments based on substrings
        if (lowerName.contains("fasting") || lowerName.contains("tsom")) {
            fastingFriendly = true
            veganFriendly = true
            tags.add("fasting")
        }
        if (lowerName.contains("meat") || lowerName.contains("siga")) {
            fastingFriendly = false
            veganFriendly = false
            tags.add("meat")
        }

        return MealMetadataSuggestion(
            category = category,
            fastingFriendly = fastingFriendly,
            veganFriendly = veganFriendly,
            proteinLevel = proteinLevel,
            spiceLevel = spiceLevel,
            carbLevel = carbLevel,
            oilLevel = oilLevel,
            tags = tags.toList(),
            ingredients = ingredients,
            mealRegion = mealRegion
        )
    }
}

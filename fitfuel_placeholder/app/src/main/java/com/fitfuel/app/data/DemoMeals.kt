package com.fitfuel.app.data

import com.fitfuel.app.model.Meal

/**
 * Proof-of-concept meals for the recommendation module.
 * These values are intentionally normalized for a stable demo.
 */
object DemoMeals {
    val caesarSalad = Meal(
        id = "caesar_salad",
        name = "Caesar Salad",
        calories = 350,
        protein = 12,
        tags = listOf("light", "salad", "lower-calorie"),
        emoji = "🥗",
        description = "A lighter option that fits tighter calorie budgets."
    )

    val carbonara = Meal(
        id = "carbonara",
        name = "Spaghetti Carbonara",
        calories = 500,
        protein = 18,
        tags = listOf("pasta", "comfort", "higher-calorie"),
        emoji = "🍝",
        description = "A richer pasta option with moderate protein and higher calories."
    )

    val filetMignon = Meal(
        id = "filet_mignon",
        name = "Filet Mignon",
        calories = 550,
        protein = 40,
        tags = listOf("high-protein", "meat", "premium"),
        emoji = "🥩",
        description = "A protein-forward option that fits muscle-focused days well."
    )
    val grilledChickenWrap = Meal(
        id = "grilled_chicken_wrap",
        name = "Grilled Chicken Wrap",
        calories = 450,
        protein = 32,
        tags = listOf("chicken", "lunch", "balanced"),
        emoji = "🌯",
        description = "A well-rounded midday meal with lean protein and whole grains."
    )

    val avocadoToast = Meal(
        id = "avocado_toast",
        name = "Avocado Toast",
        calories = 300,
        protein = 8,
        tags = listOf("breakfast", "vegetarian", "healthy-fats"),
        emoji = "🥑",
        description = "A trendy, quick breakfast packed with healthy fats and fiber."
    )

    val salmonBowl = Meal(
        id = "salmon_bowl",
        name = "Teriyaki Salmon Bowl",
        calories = 620,
        protein = 35,
        tags = listOf("fish", "rice", "omega-3"),
        emoji = "🐟",
        description = "A nutrient-dense bowl featuring fatty fish and steamed veggies."
    )

    val lentilSoup = Meal(
        id = "lentil_soup",
        name = "Hearty Lentil Soup",
        calories = 280,
        protein = 16,
        tags = listOf("vegan", "soup", "high-fiber"),
        emoji = "🥣",
        description = "A comforting, plant-based bowl loaded with complex carbs and fiber."
    )

    val beefStirFry = Meal(
        id = "beef_stir_fry",
        name = "Beef & Broccoli Stir Fry",
        calories = 480,
        protein = 30,
        tags = listOf("beef", "vegetables", "quick"),
        emoji = "🍳",
        description = "A fast, savory weeknight staple with crisp veggies and tender steak."
    )

    val yogurtParfait = Meal(
        id = "yogurt_parfait",
        name = "Greek Yogurt Parfait",
        calories = 220,
        protein = 20,
        tags = listOf("snack", "breakfast", "sweet"),
        emoji = "🍨",
        description = "A sweet, protein-rich snack layered with berries and granola."
    )

    val veggieBurger = Meal(
        id = "veggie_burger",
        name = "Black Bean Veggie Burger",
        calories = 410,
        protein = 15,
        tags = listOf("vegetarian", "burger", "plant-based"),
        emoji = "🍔",
        description = "A satisfying meat-free alternative that goes great with sweet potato fries."
    )

    val proteinPancakes = Meal(
        id = "protein_pancakes",
        name = "Protein Pancakes",
        calories = 380,
        protein = 28,
        tags = listOf("breakfast", "high-protein", "sweet"),
        emoji = "🥞",
        description = "A macro-friendly take on a breakfast classic to start the day strong."
    )

    val tunaSalad = Meal(
        id = "tuna_salad",
        name = "Classic Tuna Salad",
        calories = 340,
        protein = 26,
        tags = listOf("fish", "lower-carb", "quick"),
        emoji = "🥫",
        description = "A simple, low-carb staple that takes minutes to prepare."
    )

    val margheritaPizza = Meal(
        id = "margherita_pizza",
        name = "Margherita Pizza",
        calories = 750,
        protein = 24,
        tags = listOf("pizza", "comfort", "higher-calorie"),
        emoji = "🍕",
        description = "A delicious Italian classic perfect for a weekend cheat meal or refeed."
    )

    val spicyTunaRoll = Meal(
        id = "spicy_tuna_roll",
        name = "Spicy Tuna Roll",
        calories = 320,
        protein = 14,
        tags = listOf("sushi", "fish", "lighter-option"),
        emoji = "🍣",
        description = "A light and flavorful roll perfect for a quick bite."
    )

    val chickenTikkaMasala = Meal(
        id = "chicken_tikka_masala",
        name = "Chicken Tikka Masala",
        calories = 550,
        protein = 38,
        tags = listOf("curry", "chicken", "comfort"),
        emoji = "🍛",
        description = "A rich and warming curry packed with protein."
    )

    val berryOatmeal = Meal(
        id = "berry_oatmeal",
        name = "Berry Oatmeal",
        calories = 250,
        protein = 7,
        tags = listOf("breakfast", "vegan", "high-fiber"),
        emoji = "🥣",
        description = "A warm, heart-healthy start to your morning."
    )

    val steakTacos = Meal(
        id = "steak_tacos",
        name = "Street Steak Tacos",
        calories = 450,
        protein = 28,
        tags = listOf("beef", "mexican", "quick"),
        emoji = "🌮",
        description = "Savory grilled steak on corn tortillas with fresh cilantro and onion."
    )

    val quinoaSalad = Meal(
        id = "quinoa_salad",
        name = "Mediterranean Quinoa Salad",
        calories = 310,
        protein = 10,
        tags = listOf("vegetarian", "salad", "light"),
        emoji = "🥗",
        description = "A refreshing, grain-based salad with feta and olives."
    )

    val porkRamen = Meal(
        id = "pork_ramen",
        name = "Tonkotsu Pork Ramen",
        calories = 600,
        protein = 25,
        tags = listOf("soup", "noodles", "comfort"),
        emoji = "🍜",
        description = "A deeply flavorful, rich broth topped with tender pork belly."
    )

    val turkeySandwich = Meal(
        id = "turkey_sandwich",
        name = "Roast Turkey Sandwich",
        calories = 400,
        protein = 30,
        tags = listOf("lunch", "sandwich", "high-protein"),
        emoji = "🥪",
        description = "A classic deli favorite loaded with lean meat."
    )

    val eggplantParmesan = Meal(
        id = "eggplant_parmesan",
        name = "Eggplant Parmesan",
        calories = 480,
        protein = 18,
        tags = listOf("vegetarian", "italian", "comfort"),
        emoji = "🍆",
        description = "Breaded and baked eggplant layered with marinara and melted cheese."
    )

    val smoothieBowl = Meal(
        id = "smoothie_bowl",
        name = "Acai Smoothie Bowl",
        calories = 290,
        protein = 6,
        tags = listOf("breakfast", "fruit", "sweet"),
        emoji = "🥥",
        description = "A vibrant, refreshing bowl topped with fresh fruit and seeds."
    )

    val shrimpSkewers = Meal(
        id = "shrimp_skewers",
        name = "Grilled Shrimp Skewers",
        calories = 200,
        protein = 24,
        tags = listOf("seafood", "lower-calorie", "high-protein"),
        emoji = "🍤",
        description = "Incredibly lean and protein-dense, perfect for cutting phases."
    )
    val all = listOf(
        caesarSalad,
        carbonara,
        filetMignon,
        grilledChickenWrap,
        avocadoToast,
        salmonBowl,
        lentilSoup,
        beefStirFry,
        yogurtParfait,
        veggieBurger,
        proteinPancakes,
        tunaSalad,
        margheritaPizza,
        spicyTunaRoll,
        chickenTikkaMasala,
        berryOatmeal,
        steakTacos,
        quinoaSalad,
        porkRamen,
        turkeySandwich,
        eggplantParmesan,
        smoothieBowl,
        shrimpSkewers
    )
}

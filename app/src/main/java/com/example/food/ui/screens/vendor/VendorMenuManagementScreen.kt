package com.example.food.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food.core.util.Resource
import com.example.food.data.model.Meal
import com.example.food.data.model.MealFilters
import com.example.food.ui.components.TopNavBar
import com.example.food.ui.viewmodel.MealViewModel
import com.example.food.ui.viewmodel.UserViewModel
import com.example.food.data.model.MealTime
import com.example.food.data.model.CuisineType
import com.example.food.data.model.EthiopianFoodCategory
import com.example.food.data.model.OilLevel
import com.example.food.data.model.SpiceLevel
import com.example.food.data.model.ProteinLevel
import kotlinx.coroutines.launch

@Composable
fun VendorMenuManagementScreen(
    userViewModel: UserViewModel,
    mealViewModel: MealViewModel,
    onNavigateBack: () -> Unit
) {
    val user by userViewModel.user.collectAsState()
    val mealsState by mealViewModel.mealsState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var mealToEdit by remember { mutableStateOf<Meal?>(null) }
    var mealToDelete by remember { mutableStateOf<Meal?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(user) {
        user?.let {
            mealViewModel.updateFilters(MealFilters(vendorId = it.userId))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0F0F0F),
        topBar = {
            TopNavBar(
                title = "Menu Management",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFF16B24),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val state = mealsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFF16B24))
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message ?: "Error loading meals", color = Color.Red)
                    }
                }
                is Resource.Success -> {
                    val meals = state.data ?: emptyList()
                    if (meals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "You haven't added any meals yet", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(meals) { meal ->
                                VendorMealItem(
                                    meal = meal,
                                    onToggleAvailability = { isAvailable ->
                                        mealViewModel.updateMeal(meal.copy(isAvailable = isAvailable)) { result ->
                                            if (result is Resource.Error) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(result.message ?: "Failed to update availability")
                                                }
                                            }
                                        }
                                    },
                                    onEdit = { mealToEdit = meal },
                                    onDelete = { mealToDelete = meal }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || mealToEdit != null) {
        MealFormDialog(
            initialMeal = mealToEdit,
            onDismiss = { 
                showAddDialog = false
                mealToEdit = null
            },
            onConfirm = { name, price, category, foodType, dietType, fasting, vegan, spice, protein, mealTime, tags, ingredients, mealRegion ->
                user?.let { vendor ->
                    if (mealToEdit != null) {
                        val updatedMeal = mealToEdit!!.copy(
                            name = name,
                            price = price,
                            category = category.name,
                            foodType = foodType,
                            dietType = dietType,
                            fastingFriendly = fasting,
                            veganFriendly = vegan,
                            spiceLevel = spice,
                            proteinLevel = protein,
                            mealTime = listOf(mealTime),
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            ingredients = ingredients,
                            mealRegion = mealRegion
                        )
                        mealViewModel.updateMeal(updatedMeal) { result ->
                            if (result is Resource.Success) {
                                mealToEdit = null
                                scope.launch { snackbarHostState.showSnackbar("Meal updated successfully!") }
                            } else if (result is Resource.Error) {
                                scope.launch { snackbarHostState.showSnackbar(result.message ?: "Failed to update meal") }
                            }
                        }
                    } else {
                        val newMeal = Meal(
                            name = name,
                            price = price,
                            category = category.name,
                            vendorId = vendor.userId,
                            businessName = "", // Will be set by UseCase
                            imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop",
                            foodType = foodType,
                            dietType = dietType,
                            fastingFriendly = fasting,
                            veganFriendly = vegan,
                            spiceLevel = spice,
                            proteinLevel = protein,
                            mealTime = listOf(mealTime),
                            tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                            ingredients = ingredients,
                            mealRegion = mealRegion
                        )
                        mealViewModel.createMeal(vendor, newMeal) { result ->
                            if (result is Resource.Success) {
                                showAddDialog = false
                                scope.launch { snackbarHostState.showSnackbar("Meal added successfully!") }
                            } else if (result is Resource.Error) {
                                scope.launch { snackbarHostState.showSnackbar(result.message ?: "Failed to add meal") }
                            }
                        }
                    }
                }
            }
        )
    }

    if (mealToDelete != null) {
        AlertDialog(
            onDismissRequest = { mealToDelete = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Confirm Delete", color = Color.White) },
            text = { Text("Are you sure you want to delete '${mealToDelete!!.name}'? This action cannot be undone.", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    val id = mealToDelete!!.id
                    mealToDelete = null
                    mealViewModel.deleteMeal(id) { result ->
                        if (result is Resource.Success) {
                            scope.launch { snackbarHostState.showSnackbar("Meal deleted successfully") }
                        } else if (result is Resource.Error) {
                            scope.launch { snackbarHostState.showSnackbar(result.message ?: "Failed to delete meal") }
                        }
                    }
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mealToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun MealFormDialog(
    initialMeal: Meal?,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, EthiopianFoodCategory, com.example.food.data.model.FoodType, com.example.food.data.model.DietType, Boolean, Boolean, SpiceLevel, ProteinLevel, MealTime, String, List<String>, String) -> Unit
) {
    var name by remember { mutableStateOf(initialMeal?.name ?: "") }
    var price by remember { mutableStateOf(initialMeal?.price?.toString() ?: "") }
    
    var category by remember { 
        mutableStateOf(
            try { 
                EthiopianFoodCategory.valueOf(initialMeal?.category ?: "GENERAL") 
            } catch (e: Exception) { EthiopianFoodCategory.GENERAL }
        ) 
    }
    
    var foodType by remember { mutableStateOf(initialMeal?.foodType ?: com.example.food.data.model.FoodType.NON_FASTING) }
    var dietType by remember { mutableStateOf(initialMeal?.dietType ?: com.example.food.data.model.DietType.MEAT) }
    var fastingFriendly by remember { mutableStateOf(initialMeal?.fastingFriendly ?: false) }
    var veganFriendly by remember { mutableStateOf(initialMeal?.veganFriendly ?: false) }
    var spiceLevel by remember { mutableStateOf(initialMeal?.spiceLevel ?: SpiceLevel.MEDIUM) }
    var proteinLevel by remember { mutableStateOf(initialMeal?.proteinLevel ?: ProteinLevel.MEDIUM) }
    var mealTime by remember { mutableStateOf(initialMeal?.mealTime?.firstOrNull() ?: MealTime.LUNCH) }
    var tags by remember { mutableStateOf(initialMeal?.tags?.joinToString(", ") ?: "") }
    var ingredients by remember { mutableStateOf<List<String>>(initialMeal?.ingredients ?: emptyList()) }
    var mealRegion by remember { mutableStateOf(initialMeal?.mealRegion ?: "Ethiopia") }
    
    var spiceExpanded by remember { mutableStateOf(false) }
    var proteinExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var foodTypeExpanded by remember { mutableStateOf(false) }
    var dietTypeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMeal == null) "Add New Meal" else "Edit Meal", color = Color.White) },
        containerColor = Color(0xFF1A1A1A),
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it 
                            if (it.length > 2 && initialMeal == null) {
                                val suggestion = com.example.food.domain.usecase.EthiopianFoodKnowledge.suggestMetadataForMeal(it)
                                category = suggestion.category
                                fastingFriendly = suggestion.fastingFriendly
                                veganFriendly = suggestion.veganFriendly
                                foodType = suggestion.foodType
                                dietType = suggestion.dietType
                                proteinLevel = suggestion.proteinLevel
                                spiceLevel = suggestion.spiceLevel
                                tags = suggestion.tags.joinToString(", ")
                                ingredients = suggestion.ingredients
                                mealRegion = suggestion.mealRegion
                            }
                        },
                        label = { Text("Meal Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color(0xFFF16B24)
                        )
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (in ETB)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color(0xFFF16B24)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { categoryExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(category.name, color = Color.White)
                        }
                        DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            EthiopianFoodCategory.values().forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = { category = cat; categoryExpanded = false }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Classification", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    
                    Box {
                        OutlinedButton(onClick = { foodTypeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(foodType.name, color = Color.White)
                        }
                        DropdownMenu(expanded = foodTypeExpanded, onDismissRequest = { foodTypeExpanded = false }) {
                            com.example.food.data.model.FoodType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = { foodType = type; foodTypeExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        OutlinedButton(onClick = { dietTypeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(dietType.name, color = Color.White)
                        }
                        DropdownMenu(expanded = dietTypeExpanded, onDismissRequest = { dietTypeExpanded = false }) {
                            com.example.food.data.model.DietType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = { dietType = type; dietTypeExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dietary Info", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = fastingFriendly, onCheckedChange = { fastingFriendly = it })
                        Text("Fasting Friendly", color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = veganFriendly, onCheckedChange = { veganFriendly = it })
                        Text("Vegan", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Spice Level", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { spiceExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(spiceLevel.name, color = Color.White)
                        }
                        DropdownMenu(expanded = spiceExpanded, onDismissRequest = { spiceExpanded = false }) {
                            SpiceLevel.values().forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name) },
                                    onClick = { spiceLevel = level; spiceExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Protein Level", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { proteinExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(proteinLevel.name, color = Color.White)
                        }
                        DropdownMenu(expanded = proteinExpanded, onDismissRequest = { proteinExpanded = false }) {
                            ProteinLevel.values().forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name) },
                                    onClick = { proteinLevel = level; proteinExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Meal Time", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    Box {
                        OutlinedButton(onClick = { timeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(mealTime.name, color = Color.White)
                        }
                        DropdownMenu(expanded = timeExpanded, onDismissRequest = { timeExpanded = false }) {
                            MealTime.values().forEach { time ->
                                DropdownMenuItem(
                                    text = { Text(time.name) },
                                    onClick = { mealTime = time; timeExpanded = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color(0xFFF16B24)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceVal = price.toDoubleOrNull() ?: 0.0
                    onConfirm(name, priceVal, category, foodType, dietType, fastingFriendly, veganFriendly, spiceLevel, proteinLevel, mealTime, tags, ingredients, mealRegion)
                }
            ) {
                Text(if (initialMeal == null) "Add" else "Save", color = Color(0xFFF16B24))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun VendorMealItem(
    meal: Meal,
    onToggleAvailability: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = meal.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(text = meal.category, fontSize = 12.sp, color = Color(0xFFF16B24))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (meal.isAvailable) "IN STOCK" else "OUT OF STOCK",
                    color = if (meal.isAvailable) Color(0xFF4CAF50) else Color.Red,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ETB ${"%,.0f".format(meal.price)}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                
                Switch(
                    checked = meal.isAvailable,
                    onCheckedChange = onToggleAvailability,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                    )
                )
                
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Meal",
                        tint = Color.Blue.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Meal",
                        tint = Color.Red.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

package com.example.insulia.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insulia.R
import com.example.insulia.data.FoodNote

@Composable
fun DietScheduleCard(modifier: Modifier = Modifier) {
    InsuliaCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(R.string.diet_timetable_title), fontSize = 14.sp,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            MealRow(stringResource(R.string.diet_breakfast_time), stringResource(R.string.meal_breakfast), stringResource(R.string.diet_breakfast_food))
            MealRow(stringResource(R.string.diet_snack_am_time), stringResource(R.string.meal_snack), stringResource(R.string.diet_snack_am_food))
            MealRow(stringResource(R.string.diet_lunch_time), stringResource(R.string.meal_lunch), stringResource(R.string.diet_lunch_food))
            MealRow(stringResource(R.string.diet_snack_pm_time), stringResource(R.string.meal_snack), stringResource(R.string.diet_snack_pm_food))
            MealRow(stringResource(R.string.diet_dinner_time), stringResource(R.string.meal_dinner), stringResource(R.string.diet_dinner_food))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = stringResource(R.string.diet_timetable_note), fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LatestFoodNoteCard(
    note: FoodNote?,
    count: Int = if (note == null) 0 else 1,
    modifier: Modifier = Modifier,
    onViewAll: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    InsuliaCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.home_latest_food_note), fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (count > 0 && onViewAll != null) {
                    TextButton(onClick = onViewAll) {
                        Text(text = stringResource(R.string.food_notes_view_all, count), fontSize = 11.sp)
                    }
                }
            }

            if (note == null) {
                Text(text = stringResource(R.string.home_no_food_notes), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(text = stringResource(note.meal.labelRes), fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = note.note, fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (onDelete != null) {
                    TextButton(onClick = onDelete,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text(text = stringResource(R.string.food_note_delete), fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealRow(time: String, meal: String, food: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text = time, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(54.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = meal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = food, fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

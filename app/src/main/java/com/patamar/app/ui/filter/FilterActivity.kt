package com.patamar.app.ui.filter

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.patamar.app.R
import com.patamar.app.data.model.EventCategory
import com.patamar.app.databinding.ActivityFilterBinding
import com.patamar.app.databinding.ItemFilterTileBinding
import com.patamar.app.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterBinding
    private val viewModel: FilterViewModel by viewModels()

    private val radiusOptions = listOf(500, 1000, 3000, 5000)
    private val radiusLabels = listOf("500 m", "1 km", "3 km", "5 km")

    private val categoryTiles = mutableListOf<Pair<EventCategory, ItemFilterTileBinding>>()
    private val radiusTiles = mutableListOf<ItemFilterTileBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buildCategoryTiles()
        buildRadiusTiles()

        binding.tvSkip.setOnClickListener { goToMain() }
        binding.tvClear.setOnClickListener {
            viewModel.filters.value.categories.forEach { viewModel.toggleCategory(it) }
        }
        binding.btnConfirm.setOnClickListener {
            viewModel.confirm()
            goToMain()
        }

        observeState()
    }

    // Categorias em grade de 2 colunas, só preto/cinza: o selecionado inverte
    // (fundo claro, texto escuro), sem cores de categoria nem check.
    private fun buildCategoryTiles() {
        val inflater = LayoutInflater.from(this)
        binding.gridCategories.removeAllViews()
        EventCategory.values().toList().chunked(2).forEachIndexed { rowIndex, pair ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            pair.forEachIndexed { col, category ->
                val tile = ItemFilterTileBinding.inflate(inflater, row, false)
                tile.tvLabel.text = category.label
                tile.dot.visibility = View.GONE
                tile.tvCheck.visibility = View.GONE
                tile.root.setOnClickListener { viewModel.toggleCategory(category) }
                (tile.root.layoutParams as LinearLayout.LayoutParams).apply {
                    if (col == 0) marginEnd = dp(5) else marginStart = dp(5)
                }
                row.addView(tile.root)
                categoryTiles += category to tile
            }
            binding.gridCategories.addView(
                row,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { if (rowIndex > 0) topMargin = dp(10) }
            )
        }
    }

    // Distância: 4 opções lado a lado, mesmo estilo.
    private fun buildRadiusTiles() {
        val inflater = LayoutInflater.from(this)
        binding.rowRadius.removeAllViews()
        radiusOptions.forEachIndexed { index, meters ->
            val tile = ItemFilterTileBinding.inflate(inflater, binding.rowRadius, false)
            tile.tvLabel.text = radiusLabels[index]
            tile.tvLabel.gravity = Gravity.CENTER
            tile.dot.visibility = View.GONE
            tile.tvCheck.visibility = View.GONE
            tile.root.setOnClickListener { viewModel.setRadius(meters) }
            (tile.root.layoutParams as LinearLayout.LayoutParams).apply {
                if (index > 0) marginStart = dp(8)
            }
            binding.rowRadius.addView(tile.root)
            radiusTiles += tile
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filters.collect { filters ->
                    categoryTiles.forEach { (category, tile) ->
                        val selected = category in filters.categories
                        applySelected(tile, selected)
                    }
                    val radiusIndex = radiusOptions.indexOf(filters.radiusMeters).coerceAtLeast(0)
                    radiusTiles.forEachIndexed { i, tile -> applySelected(tile, i == radiusIndex) }

                    val count = filters.categories.size
                    binding.tvHint.text =
                        if (count == 0) getString(R.string.filter_hint_all)
                        else getString(R.string.filter_hint_some, count)
                    binding.tvClear.visibility = if (count == 0) View.INVISIBLE else View.VISIBLE
                }
            }
        }
    }

    private fun applySelected(tile: ItemFilterTileBinding, selected: Boolean) {
        tile.root.setCardBackgroundColor(
            ContextCompat.getColor(this, if (selected) R.color.accent else R.color.bg_elevated)
        )
        tile.root.strokeColor =
            ContextCompat.getColor(this, if (selected) R.color.accent else R.color.border_default)
        tile.tvLabel.setTextColor(
            ContextCompat.getColor(this, if (selected) R.color.on_accent else R.color.text_primary)
        )
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }
}

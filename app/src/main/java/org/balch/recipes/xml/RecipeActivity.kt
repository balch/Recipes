package org.balch.recipes.xml

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import org.balch.recipes.databinding.ActivityRecipeBinding
import org.balch.recipes.xml.features.ideas.IdeasFragment


@AndroidEntryPoint
class RecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                // Replace whatever is in the fragment_container view with this fragment
                add(binding.root, IdeasFragment.newInstance(), "Ideas")
            }
        }
    }
}
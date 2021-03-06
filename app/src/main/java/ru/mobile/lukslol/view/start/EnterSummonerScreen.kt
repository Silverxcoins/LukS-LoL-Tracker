package ru.mobile.lukslol.view.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.screen_enter_summoner.*
import kotlinx.coroutines.launch
import ru.mobile.lukslol.R
import ru.mobile.lukslol.databinding.ScreenEnterSummonerBinding
import ru.mobile.lukslol.di.Components
import ru.mobile.lukslol.domain.error.NetworkError
import ru.mobile.lukslol.util.setEndCancelListener
import ru.mobile.lukslol.util.view.hideKeyboard
import ru.mobile.lukslol.view.Screen
import ru.mobile.lukslol.view.start.EnterSummonerAction.*
import ru.mobile.lukslol.view.start.EnterSummonerMutation.BackPressed
import ru.mobile.lukslol.view.start.EnterSummonerMutation.GoClick
import javax.inject.Inject

class EnterSummonerScreen : Screen() {

    companion object {
        private const val ANIMATION_DURATION = 300L
    }

    @Inject
    lateinit var viewModel: EnterSummonerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel(EnterSummonerViewModel::class)
        Components.enterSummonerComponent.get().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = inflateBinding<ScreenEnterSummonerBinding>(inflater, R.layout.screen_enter_summoner, container)
        binding.screen = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
    }

    override fun onBackPressed(): Boolean {
        return if (navController().currentDestination?.id == R.id.chooseRegionFragment) {
            activity?.finish()
            true
        } else {
            viewModel.mutate(BackPressed)
            false
        }
    }

    fun goBtnClick() {
        val currentDestination = EnterSummonerDestination.fromId(navController().currentDestination!!.id)
        viewModel.mutate(GoClick(currentDestination!!))
    }

    private fun initViewModel() {
        launch {
            viewModel.actions { action ->
                when (action) {
                    is MoveForward -> navController().navigate(R.id.action_chooseRegionFragment_to_enterSummonerNameFragment)
                    is HideKeyboard -> view?.hideKeyboard()
                    is Finish -> startFinishAnimation()
                    is ShowErrorSnack -> {
                        val errorTextRes = when (action.e) {
                            NetworkError.NOT_FOUND -> R.string.enter_summoner_not_found
                            NetworkError.CONNECTION -> R.string.error_network_connection
                            else -> R.string.error_unknown
                        }
                        Snackbar.make(requireView(), errorTextRes, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navController() = Navigation.findNavController(enter_summoner_navigation_fragment)

    private fun startFinishAnimation() {
        enter_summoner_image.animate()
            .translationY(-enter_summoner_image.height.toFloat())
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .start()
        enter_summoner_content.animate()
            .translationY(enter_summoner_content.height.toFloat())
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .setEndCancelListener { topNavController.popBackStack() }
            .start()
    }
}
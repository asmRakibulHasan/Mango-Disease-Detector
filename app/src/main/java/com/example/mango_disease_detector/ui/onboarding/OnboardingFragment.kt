package com.example.mango_disease_detector.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mango_disease_detector.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = arguments?.getString(ARG_TITLE)
        binding.tvDesc.text = arguments?.getString(ARG_DESC)
        arguments?.getInt(ARG_IMAGE)?.let { binding.ivBadge.setImageResource(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"
        private const val ARG_IMAGE = "image"

        fun newInstance(title: String, desc: String, imageRes: Int) = OnboardingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_DESC, desc)
                putInt(ARG_IMAGE, imageRes)
            }
        }
    }
}

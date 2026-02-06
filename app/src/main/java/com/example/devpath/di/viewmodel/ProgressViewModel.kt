package com.example.devpath.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.devpath.data.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    val progressRepository: ProgressRepository
) : ViewModel()
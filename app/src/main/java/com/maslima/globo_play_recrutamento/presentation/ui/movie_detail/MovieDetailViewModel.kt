package com.maslima.globo_play_recrutamento.presentation.ui.movie_detail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maslima.globo_play_recrutamento.domain.model.Movie
import com.maslima.globo_play_recrutamento.presentation.components.MovieEvent
import com.maslima.globo_play_recrutamento.repository.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Named

const val STATE_KEY_MOVIE = "movie.state.movie.key"

@ExperimentalCoroutinesApi
class MovieDetailViewModel
@ViewModelInject
constructor(
    private val movieRepository: MovieRepository,
    @Assisted private val state: SavedStateHandle,
) : ViewModel() {

    val movie: MutableState<Movie?> = mutableStateOf(null)

    val loading = mutableStateOf(false)

    val movieExists: MutableState<Boolean> = mutableStateOf(false)

    init {
        state.get<Int>(STATE_KEY_MOVIE)?.let { movieId ->
            onTriggerEvent(MovieEvent.GetMovieEvent(movieId))
        }
        onTriggerEvent(MovieEvent.SelectSpecificMovie)
    }

    fun onTriggerEvent(event: MovieEvent) {
        viewModelScope.launch {
            try {
                when (event) {
                    is MovieEvent.GetMovieEvent -> {
                        if (movie.value == null) {
                            getMovie(event.id)
                        }
                    }
                    is MovieEvent.AddMovieEvent -> {
                        addMovie()
                    }
                    is MovieEvent.DeleteMovieEvent -> {
                        removeMovie()
                    }
                    MovieEvent.SelectSpecificMovie -> selectSpecificMovie()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun selectSpecificMovie() {
        loading.value = true
        this.movie.value?.let {
            val movie1 = movieRepository.getMovie(it.id)
            movieExists.value = movie1 != null
        }

        loading.value = false
    }

    private suspend fun removeMovie() {
        loading.value = true
        this.movie.value?.let {
            movieRepository.deleteFavorite(it)
        }
        loading.value = false
    }

    private suspend fun getMovie(id: Int) {
        loading.value = true

        val movie = movieRepository.getMovie(movieId = id)
        this.movie.value = movie

        state.set(STATE_KEY_MOVIE, movie?.id)

        loading.value = false
    }

    private suspend fun addMovie() {
        loading.value = true
        this.movie.value?.let {
            movieRepository.insertFavorite(it)
        }
        loading.value = false
    }
}
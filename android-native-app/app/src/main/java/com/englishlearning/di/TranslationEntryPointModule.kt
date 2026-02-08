package com.englishlearning.di

import com.englishlearning.data.translation.TranslationService
import com.englishlearning.ui.components.TranslationServiceEntryPoint
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslationEntryPointModule {

    @Binds
    @Singleton
    abstract fun bindTranslationServiceEntryPoint(
        translationServiceImpl: TranslationServiceImpl
    ): TranslationServiceEntryPoint
}

/**
 * TranslationServiceEntryPoint的实现
 */
class TranslationServiceImpl @javax.inject.Inject constructor(
    private val translationService: TranslationService
) : TranslationServiceEntryPoint {
    override fun getTranslationService(): TranslationService {
        return translationService
    }
}

package org.koitharu.verter

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.koitharu.verter.core.db.AppDatabase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

	@Provides
	fun provideDatabase(
		@ApplicationContext context: Context,
	): AppDatabase = AppDatabase(context)
}
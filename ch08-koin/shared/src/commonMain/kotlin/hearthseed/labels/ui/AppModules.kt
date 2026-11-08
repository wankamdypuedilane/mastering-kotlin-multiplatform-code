package hearthseed.labels.ui

import hearthseed.labels.coreModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::LabelViewModel)
}

/** Every module of the app: for startKoin, KoinApplication and tests. */
fun appModules(): List<Module> = listOf(coreModule, uiModule, platformModule)

/** The single entry point used by Android and iOS. */
fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(appModules())
    }
}

package hearthseed.labels

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/** Qualifier for the library name, a plain String in the graph. */
val LibraryName = named("libraryName")

/** The same graph as ManualContainer, declared for Koin. */
val coreModule = module {
    single(LibraryName) { "Larkmoor Commons" }
    single<LabelFormatter> { StandardLabelFormatter(get(LibraryName)) }
    singleOf(::DemoPacketRepository) bind PacketRepository::class
    factoryOf(::LabelPrinter)
    factory { (columns: Int) -> LabelSheet(columns, get()) }
}

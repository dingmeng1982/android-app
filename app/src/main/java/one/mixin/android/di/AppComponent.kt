package one.mixin.android.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton
import one.mixin.android.MixinApplication
import one.mixin.android.di.worker.WorkerModule
import one.mixin.android.job.BaseJob
import one.mixin.android.job.Injector

@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class),
    (WorkerModule::class),
    (AppModule::class),
    (ActivityModule::class),
    (ServiceModule::class)])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(mixApp: MixinApplication)

    fun inject(baseJob: BaseJob)

    fun inject(injector: Injector)
}

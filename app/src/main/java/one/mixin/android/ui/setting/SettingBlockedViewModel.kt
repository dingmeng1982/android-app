package one.mixin.android.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import one.mixin.android.api.service.UserService
import one.mixin.android.repository.AccountRepository
import one.mixin.android.repository.UserRepository
import one.mixin.android.vo.User
import one.mixin.android.vo.UserRelationship

class SettingBlockedViewModel @Inject
internal constructor(
    private val userService: UserService,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun blockingUsers(scopeProvider: ScopeProvider): LiveData<List<User>> {
        userService.blockingUsers().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .autoDisposable(scopeProvider).subscribe({
                if (it.isSuccess) {
                    it.data?.let {
                        for (user in it) {
                            user.relationship = UserRelationship.BLOCKING.name
                            userRepository.upsertBlock(user)
                        }
                    }
                }
            }, {})
        return accountRepository.findUsersByType(UserRelationship.BLOCKING.name)
    }
}

package one.mixin.android.ui.conversation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import com.uber.autodispose.autoDisposable
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_transactions_user.*
import kotlinx.android.synthetic.main.view_title.view.*
import one.mixin.android.Constants.ARGS_USER_ID
import one.mixin.android.R
import one.mixin.android.extension.addFragment
import one.mixin.android.extension.enqueueOneTimeNetworkWorkRequest
import one.mixin.android.extension.withArgs
import one.mixin.android.job.MixinJobManager
import one.mixin.android.ui.common.BaseFragment
import one.mixin.android.ui.wallet.TransactionFragment
import one.mixin.android.ui.wallet.TransactionsFragment
import one.mixin.android.ui.wallet.WalletViewModel
import one.mixin.android.ui.wallet.adapter.OnSnapshotListener
import one.mixin.android.ui.wallet.adapter.SnapshotListAdapter
import one.mixin.android.vo.SnapshotItem
import one.mixin.android.worker.RefreshUserSnapshotsWorker

class UserTransactionsFragment : BaseFragment(), OnSnapshotListener {

    companion object {
        const val TAG = "UserTransactionsFragment"

        fun newInstance(userId: String) = UserTransactionsFragment().withArgs {
            putString(ARGS_USER_ID, userId)
        }
    }

    @Inject
    lateinit var jobManager: MixinJobManager
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val walletViewModel: WalletViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(WalletViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_transactions_user, container, false).apply {
            isClickable = true
        }

    private val adapter by lazy {
        SnapshotListAdapter()
    }

    private val userId by lazy {
        arguments!!.getString(ARGS_USER_ID)!!
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        transactions_rv.addItemDecoration(StickyRecyclerHeadersDecoration(adapter))
        title_view.right_animator.visibility = View.GONE
        title_view.left_ib.setOnClickListener { activity?.onBackPressed() }
        WorkManager.getInstance(requireContext()).enqueueOneTimeNetworkWorkRequest<RefreshUserSnapshotsWorker>(
            workDataOf(RefreshUserSnapshotsWorker.USER_ID to userId))
        adapter.listener = this
        transactions_rv.adapter = adapter
        walletViewModel.snapshotsByUserId(userId).observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun <T> onNormalItemClick(item: T) {
        val snapshot = item as SnapshotItem
        walletViewModel.getAssetItem(snapshot.assetId).autoDisposable(stopScope).subscribe({ assetItem ->
            assetItem.let {
                try {
                    view?.findNavController()?.navigate(R.id.action_user_transactions_to_transaction,
                        Bundle().apply {
                            putParcelable(TransactionFragment.ARGS_SNAPSHOT, snapshot)
                            putParcelable(TransactionsFragment.ARGS_ASSET, it)
                        })
                } catch (e: IllegalStateException) {
                    val fragment = TransactionFragment.newInstance(snapshot, it)
                    activity?.addFragment(this@UserTransactionsFragment, fragment, TransactionFragment.TAG)
                }
            }
        }, {})
    }

    override fun onUserClick(userId: String) {
        // Do nothing, avoid recursively calling this page.
    }
}

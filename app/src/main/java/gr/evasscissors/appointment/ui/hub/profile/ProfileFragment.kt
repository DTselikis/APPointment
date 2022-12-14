package gr.evasscissors.appointment.ui.hub.profile

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import gr.evasscissors.appointment.R
import gr.evasscissors.appointment.data.FB_PACKAGE_NAME
import gr.evasscissors.appointment.data.Gender
import gr.evasscissors.appointment.data.RE_AUTH_NAV_KEY
import gr.evasscissors.appointment.databinding.FragmentProfileBinding
import id.zelory.compressor.Compressor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private val sharedViewModel: gr.evasscissors.appointment.NavViewModel by navGraphViewModels(R.id.auth_nav_graph)
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(sharedViewModel.user)
    }

    private lateinit var binding: FragmentProfileBinding
    private lateinit var photoUri: Uri

    private lateinit var callbackManager: CallbackManager

    private val openGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.profilePicProgress.show()

                lifecycleScope.launch {
                    try {
                        val compressedFile = Compressor.compress(requireContext(), it.toFile()!!)
                        photoUri = uri
                        viewModel.storeImageToFirebase(compressedFile)
                    } catch (e: NoSuchFileException) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.compress_img_err),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onCancel() {
                showSnackBar(
                    getString(R.string.fb_login_canceled),
                    R.color.md_theme_dark_errorContainer
                )
            }

            override fun onError(error: FacebookException) {
                showSnackBar(getString(R.string.fb_login_err), R.color.md_theme_dark_errorContainer)
            }

            override fun onSuccess(result: LoginResult) {
                GraphRequest.newMeRequest(result.accessToken) { obj, _ ->
                    viewModel.storeFBProfileInfo(obj!!.getString("id"), obj.getString("name"))

                    val url = obj.getJSONObject("picture").getJSONObject("data").getString("url")
                    askToUseFacebookProfilePic(url)
                }.apply {
                    parameters =
                        Bundle().apply { putString("fields", "id,name,picture.height(2048)") }
                    executeAsync()
                }

                viewModel.linkFacebookAccount(result.accessToken.token)
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, null, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@ProfileFragment.viewModel
            profileFragment = this@ProfileFragment
            if (this@ProfileFragment.viewModel.user.activeNotifications!! > 0)
                notificationBadge.visibility = View.VISIBLE

            emailEditText.apply {
                doOnTextChanged { text, _, _, _ ->
                    if (text.toString() != this@ProfileFragment.viewModel.user.email) {
                        showSaveBtn(emailEdit, fbBtn)
                    } else if (text.toString() == this@ProfileFragment.viewModel.user.email) {
                        hideSaveBtn(fbBtn, emailEdit)
                    }
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        saveEditsBtn.setOnClickListener {
                            binding.generalProgress.show()

                            this@ProfileFragment.viewModel.verifyNewEmail()
                        }
                    }
                }
            }

            phoneEditText.apply {
                doOnTextChanged { text, _, _, _ ->
                    val currentPhone = this@ProfileFragment.viewModel.user.phone
                    if (text.toString() != currentPhone) {
                        showSaveBtn(phoneEdit, emailEdit)
                    } else if (text.toString() == currentPhone) {
                        hideSaveBtn(emailEdit, phoneEdit)
                    }
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        saveEditsBtn.setOnClickListener {
                            binding.generalProgress.show()

                            this@ProfileFragment.viewModel.storeUpdatedPhone()
                        }
                    }
                }
            }

            genderGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val gender = when (checkedId) {
                        R.id.gender_female ->
                            Gender.FEMALE
                        R.id.gender_male ->
                            Gender.MALE
                        else ->
                            Gender.ANY
                    }

                    if (gender.code != this@ProfileFragment.viewModel.gender) {
                        binding.generalProgress.show()

                        this@ProfileFragment.viewModel.updateGender(gender) { updated ->
                            binding.generalProgress.hide()

                            val field = getString(R.string.gender)
                            val message = if (updated) getString(
                                R.string.value_updated,
                                field
                            ) else getString(R.string.value_not_updated, field)

                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        if (viewModel.checkFacebookAccountLinked())
            binding.fbBtn.setOnClickListener { fbLogout() }
        else
            binding.fbBtn.setOnClickListener { fbLogin() }

        viewModel.gender?.let {
            checkGenderButton(it)
        }

        observePicUploaded()
        observeVerificationEmailSent()
        observeReAuthFinished()
        observeReAuthRequirement()
        observeUpdatedPhoneStored()
        observeFbProfileInfoStored()
        observeFacebookAccountLinked()
        observeAccountUnlinked()
        observeFacebookAccountIsLinked()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        sharedViewModel.user = viewModel.getUpdatedUser()
    }

    fun pickImage() {
        openGallery.launch("image/*")
    }

    fun openNotificationsBottomSheet() {
        val action =
            ProfileFragmentDirections.actionProfileFragmentToManageNotificationsFragment(
                viewModel.user.uid!!
            )
        findNavController().navigate(action)

        activity?.findViewById<BottomNavigationView>(R.id.bottom_nav_view)
            ?.removeBadge(R.id.profileFragment)
        binding.notificationBadge.visibility = View.GONE
    }

    private fun observeReAuthFinished() {
        findNavController().previousBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            savedStateHandle.getLiveData<Boolean>(RE_AUTH_NAV_KEY)
                .observe(viewLifecycleOwner) { reAuthFinished ->
                    if (reAuthFinished) viewModel.verifyNewEmail()
                    savedStateHandle.remove<Boolean>(RE_AUTH_NAV_KEY)
                }
        }
    }

    private fun observeReAuthRequirement() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.needsReAuth.collectLatest { needsReAuth ->
                if (needsReAuth) {
                    val action =
                        ProfileFragmentDirections.actionProfileFragmentToReAuthFragment(
                            viewModel.user.email!!
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun observeVerificationEmailSent() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.verificationEmailSent.collectLatest { sent ->
                if (sent) {
                    informUser()
                }
            }
        }
    }

    private fun informUser() {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.verification_dialog_title))
                .setMessage(
                    getString(
                        R.string.verification_dialog_verify_email,
                        viewModel.email.value
                    )
                )
                .setOnDismissListener {
                    signOut()
                }
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun observeUpdatedPhoneStored() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.updatedPhoneStored.collectLatest { stored ->
                binding.generalProgress.hide()

                val (text, color) = when (stored) {
                    true -> {
                        hideSaveBtn(binding.emailEdit, binding.phoneEdit)
                        Pair(getString(R.string.phone_updated), R.color.md_theme_dark_secondary)
                    }

                    else -> Pair(
                        getString(R.string.email_not_updated),
                        R.color.md_theme_dark_errorContainer
                    )
                }
                showSnackBar(text, color)
            }
        }
    }

    private fun observePicUploaded() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.picUploaded.collectLatest { stored ->
                binding.profilePicProgress.hide()

                val (text, color) = when (stored) {
                    true -> Pair(
                        getString(R.string.profile_pic_saved),
                        getColor(R.color.md_theme_dark_secondary)
                    )
                    false -> Pair(
                        getString(R.string.profile_pic__not_saved),
                        getColor(R.color.md_theme_dark_errorContainer)
                    )
                }

                Snackbar.make(binding.fbBtn, text, Snackbar.LENGTH_LONG)
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                    .setBackgroundTint(color)
                    .show()
            }
        }
    }

    private fun observeFbProfileInfoStored() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fbProfileInfoStored.collectLatest { stored ->
                binding.generalProgress.hide()

                val (text, color) = if (stored) {
                    Pair(getString(R.string.fb_login_success), R.color.md_theme_dark_secondary)
                } else {
                    Pair(getString(R.string.fb_login_err), R.color.md_theme_dark_errorContainer)
                }

                showSnackBar(text, color)
            }
        }
    }

    private fun observeFacebookAccountLinked() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.fbProfileLinked.collectLatest { linked ->
                val text =
                    if (linked) getString(R.string.fb_login_account_linked) else getString(R.string.fb_login_account_not_linked)

                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeFacebookAccountIsLinked() {
        viewModel.isFacebookAccountLinked.observe(viewLifecycleOwner) { linked ->
            if (linked) {
                binding.fbBtn.setOnClickListener { fbLogout() }
            } else {
                binding.fbBtn.setOnClickListener { fbLogin() }
            }
        }
    }

    private fun observeAccountUnlinked() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.accountUnlinked.collectLatest { unliked ->
                val text =
                    if (unliked) getString(R.string.fb_login_account_unlink_success) else getString(
                        R.string.fb_login_account_unlink_failure
                    )

                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSnackBar(message: String, @ColorRes color: Int) {
        Snackbar.make(binding.saveEditsBtn, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(color))
            .show()
    }

    private fun showSaveBtn(top: View, below: View?) {
        binding.saveEditsBtn.apply {
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = top.id
            }
            visibility = View.VISIBLE
        }

        below?.let {
            it.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = binding.saveEditsBtn.id
            }
        }
    }

    private fun hideSaveBtn(below: View, top: View) {
        below.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topToBottom = top.id
        }
        binding.saveEditsBtn.visibility = View.GONE
    }

    private fun fbLogin() {
        if (isFacebookAppInstalled()) {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, callbackManager, listOf("public_profile"))
        } else {
            showFacebookAppMissingDialog()
        }
    }

    private fun fbLogout() {
        viewModel.unlinkFacebookAccount()
        LoginManager.getInstance().logOut()
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        AuthUI.getInstance().signOut(requireContext())
        findNavController().navigate(R.id.action_profileFragment_to_authFragment)
    }

    private fun getColor(@ColorRes color: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(color, requireActivity().theme)
        } else {
            ContextCompat.getColor(requireContext(), color)
        }
    }

    private fun isFacebookAppInstalled(): Boolean {
        return try {
            context?.packageManager?.getPackageInfo(FB_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun showFacebookAppMissingDialog() {
        activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle(getString(R.string.fb_login_app_missing_title))
                setMessage(getString(R.string.fb_login_install_app_msg))
                setIcon(R.drawable.facebook_logo)
                setPositiveButton(getString(R.string.fb_login_install_app)) { dialog, _ ->
                    openPlayStore(FB_PACKAGE_NAME)
                }
                setNegativeButton(getString(R.string.cancel)) { _, _ ->
                }
            }
        }
            ?.create()
            ?.show()
    }

    private fun openPlayStore(packageName: String) {
        val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            setPackage(packageName)
        }

        startActivity(playStoreIntent)
    }

    private fun askToUseFacebookProfilePic(url: String) {
        activity?.let {
            AlertDialog.Builder(it).apply {
                setTitle(getString(R.string.fb_login_profile_pic_title))
                setMessage(getString(R.string.fb_login_profile_pic_msg))
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    binding.generalProgress.show()

                    viewModel.storeProfilePicToFirebase(url)
                }
                setNegativeButton(getString(R.string.no)) { _, _ ->

                }
            }
                .show()
        }
    }

    private fun checkGenderButton(genderCode: String) {
        binding.apply {
            val id = when (genderCode) {
                Gender.FEMALE.code -> R.id.gender_female
                Gender.MALE.code -> R.id.gender_male
                else -> R.id.gender_any
            }

            genderGroup.check(id)
        }
    }

    private fun Uri.toFile(): File? {
        requireActivity().contentResolver.openInputStream(this)?.let { inputSteam ->
            val tmpFile = File.createTempFile("profile", "pic")
            val fileOutputStream = FileOutputStream(tmpFile)

            inputSteam.copyTo(fileOutputStream)
            inputSteam.close()
            fileOutputStream.close()

            return tmpFile
        }

        return null
    }
}
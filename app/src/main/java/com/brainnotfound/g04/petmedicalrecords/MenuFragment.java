package com.brainnotfound.g04.petmedicalrecords;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brainnotfound.g04.petmedicalrecords.pets.PetsFragment;
import com.brainnotfound.g04.petmedicalrecords.module.Profile;
import com.brainnotfound.g04.petmedicalrecords.module.SaveFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class MenuFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseStorage mStorage;
    private Profile _getProfile;
    private SaveFragment saveFragment;
    private ProgressBar loadingMenu;
    private StorageReference storageReference;
    private ImageView _profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        saveFragment.setName("MenuFragment");

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
        storageReference = mStorage.getReference();
        _getProfile = Profile.getProfileInstance();
        saveFragment = SaveFragment.getSaveFragmentInstance();
        loadingMenu = getView().findViewById(R.id.menu_loading);
        String _userUid = mAuth.getCurrentUser().getUid();

        _profileImage = getView().findViewById(R.id.menu_profileImages);
        invisibleMenu();
        getProfile(_userUid);
        getCountPet(_userUid);
        initSignoutBtn();
        initProfileBtn();
        initPetsBtn();
    }

    void initSignoutBtn() {
        TextView _signoutBtn = getView().findViewById(R.id.menu_signoutBtn);
        _signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFragment.setName("LoginFragment");
                _getProfile.setFirstname(null);
                mAuth.signOut();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.main_view, new LoginFragment()).commit();
            }
        });
    }

    void initProfileBtn() {
        LinearLayout _profileLiBtn = getView().findViewById(R.id.layout_fragment_menu_profile);
        _profileLiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFragment.setName("ProfileFragment");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .addToBackStack(null)
                        .replace(R.id.main_view, new ProfileFragment()).commit();
            }
        });
    }

    void initPetsBtn() {
        LinearLayout _petsBtn = getView().findViewById(R.id.layout_fragment_menu_pet);
        _petsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFragment.setName("PetsFragment");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .addToBackStack(null)
                        .replace(R.id.main_view, new PetsFragment()).commit();
            }
        });
    }

    private void getProfile(String userUid) {
        if(_getProfile.getFirstname() == null) {
            mStore.collection("account").document(userUid)
                    .collection("profile").document(userUid)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> checkData = documentSnapshot.getData();
                        if (checkData.size() != 0) {
                            _getProfile.setFirstname(documentSnapshot.getString("firstname"));
                            _getProfile.setLastname(documentSnapshot.getString("lastname"));
                            _getProfile.setPhonenumber(documentSnapshot.getString("phonenumber"));
                            _getProfile.setAccount_type(documentSnapshot.getString("account_type"));
                            _getProfile.setUrlImage(documentSnapshot.getString("urlImage"));
                            initProfile(_getProfile);
                        }
                    }

                    storageReference.child(_getProfile.getUrlImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(getActivity()).load(uri).apply(RequestOptions.circleCropTransform()).into(_profileImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            initProfile(_getProfile);
            storageReference.child(_getProfile.getUrlImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getActivity()).load(uri).apply(RequestOptions.circleCropTransform()).into(_profileImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void getCountPet(String userUid) {
        mStore.collection("account").document(userUid)
                .collection("pets").orderBy("pet_name", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()) {
                    initCountPet(queryDocumentSnapshots);
                    loadingMenu.setVisibility(View.GONE);
                    visibleMenu();
                } else {
                    initCountPet(queryDocumentSnapshots);
                    loadingMenu.setVisibility(View.GONE);
                    visibleMenu();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initProfile(Profile _profile) {
        TextView _profileNameTxt = getView().findViewById(R.id.menu_profilename);
        _profileNameTxt.setText(_profile.getFirstname() + " " + _profile.getLastname());
    }

    private void initCountPet(QuerySnapshot queryDocumentSnapshots) {
        TextView _countPet = getView().findViewById(R.id.menu_show_count_pet);
        if(_getProfile.getAccount_type().equals("customer")) {
            _countPet.setText("จำนวนสัตว์เลี้ยงทั้งหมดของคุณคือ " + queryDocumentSnapshots.size() + " ตัว");
        } else {
            _countPet.setText("จำนวนสัตว์เลี้ยงที่ได้รับอนุญาติของคุณคือ " + queryDocumentSnapshots.size() + " ตัว");
        }
    }

    private void invisibleMenu() {
        TextView _signoutBtn = getView().findViewById(R.id.menu_signoutBtn);
        TextView _name = getView().findViewById(R.id.menu_profilename);
        TextView _countpet = getView().findViewById(R.id.menu_show_count_pet);
        LinearLayout _profile = getView().findViewById(R.id.layout_fragment_menu_profile);
        LinearLayout _pets = getView().findViewById(R.id.layout_fragment_menu_pet);
        LinearLayout _veterinary = getView().findViewById(R.id.layout_fragment_menu_veterinary);
        LinearLayout _history = getView().findViewById(R.id.layout_fragment_menu_history);
        LinearLayout _medicine = getView().findViewById(R.id.layout_fragment_menu_medicine);
        LinearLayout _request = getView().findViewById(R.id.layout_fragment_menu_request);
        LinearLayout _setting = getView().findViewById(R.id.layout_fragment_menu_setting);

        _profileImage.setVisibility(View.INVISIBLE);
        _signoutBtn.setVisibility(View.INVISIBLE);
        _name.setVisibility(View.INVISIBLE);
        _countpet.setVisibility(View.INVISIBLE);
        _profile.setVisibility(View.INVISIBLE);
        _pets.setVisibility(View.INVISIBLE);
        _veterinary.setVisibility(View.INVISIBLE);
        _history.setVisibility(View.INVISIBLE);
        _medicine.setVisibility(View.INVISIBLE);
        _request.setVisibility(View.INVISIBLE);
        _setting.setVisibility(View.INVISIBLE);
    }

    private void visibleMenu() {
        TextView _signoutBtn = getView().findViewById(R.id.menu_signoutBtn);
        TextView _name = getView().findViewById(R.id.menu_profilename);
        TextView _countpet = getView().findViewById(R.id.menu_show_count_pet);
        LinearLayout _profile = getView().findViewById(R.id.layout_fragment_menu_profile);
        LinearLayout _pets = getView().findViewById(R.id.layout_fragment_menu_pet);
        LinearLayout _veterinary = getView().findViewById(R.id.layout_fragment_menu_veterinary);
        LinearLayout _history = getView().findViewById(R.id.layout_fragment_menu_history);
        LinearLayout _medicine = getView().findViewById(R.id.layout_fragment_menu_medicine);
        LinearLayout _request = getView().findViewById(R.id.layout_fragment_menu_request);
        LinearLayout _setting = getView().findViewById(R.id.layout_fragment_menu_setting);

        _profileImage.setVisibility(View.VISIBLE);
        _signoutBtn.setVisibility(View.VISIBLE);
        _name.setVisibility(View.VISIBLE);
        _countpet.setVisibility(View.VISIBLE);
        _profile.setVisibility(View.VISIBLE);
        _pets.setVisibility(View.VISIBLE);
        if(!_getProfile.getAccount_type().equals("veterinary")) {
            _veterinary.setVisibility(View.VISIBLE);
            _history.setVisibility(View.VISIBLE);
            _medicine.setVisibility(View.VISIBLE);
            _request.setVisibility(View.VISIBLE);
            _setting.setVisibility(View.VISIBLE);
        }
    }
}

package com.brainnotfound.g04.petmedicalrecords.module.Pets;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brainnotfound.g04.petmedicalrecords.R;
import com.brainnotfound.g04.petmedicalrecords.module.SaveFragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PetInformationFragment extends Fragment {

    private Pets petInformation;
    private SaveFragment saveFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_information, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        petInformation = Pets.getGetPetsInstance();
        saveFragment = SaveFragment.getSaveFragmentInstance();
        saveFragment.setName("PetInformationFragment");
        showPetInformation(petInformation);
        initBackBtn();
    }

    private void showPetInformation(Pets pets) {
        final ImageView _imagePet = getView().findViewById(R.id.frg_pet_inf_images);
        TextView _petNameTxt = getView().findViewById(R.id.frg_pet_inf_petname);
        TextView _petTypeTxt = getView().findViewById(R.id.frg_pet_inf_type);
        TextView _petSexTxt = getView().findViewById(R.id.frg_pet_inf_sex);
        TextView _petAgeTxt = getView().findViewById(R.id.frg_pet_inf_age);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(pets.getUrlImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getActivity()).load(uri).apply(RequestOptions.circleCropTransform()).into(_imagePet);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        _petNameTxt.setText("ชื่อ : " + pets.getPet_name());
        _petTypeTxt.setText("ประเภท : " + pets.getPet_type());
        _petSexTxt.setText("เพศ : " + pets.getPet_sex());
        _petAgeTxt.setText("อายุ : " + pets.getPet_ageYear() + " ปี " + pets.getPet_ageMonth() + " เดือน " + pets.getPet_ageDay() + " วัน");
    }

    private void initBackBtn() {
        ImageView _backBtn = getView().findViewById(R.id.frg_pet_inf_backBtn);
        _backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.main_view, new PetsFragment()).commit();
            }
        });
    }
}
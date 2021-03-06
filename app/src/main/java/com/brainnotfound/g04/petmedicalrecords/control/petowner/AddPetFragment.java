package com.brainnotfound.g04.petmedicalrecords.control.petowner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.brainnotfound.g04.petmedicalrecords.MainActivity;
import com.brainnotfound.g04.petmedicalrecords.R;
import com.brainnotfound.g04.petmedicalrecords.control.HomeFragment;
import com.brainnotfound.g04.petmedicalrecords.control.LoginFragment;
import com.brainnotfound.g04.petmedicalrecords.module.Pet;
import com.brainnotfound.g04.petmedicalrecords.module.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddPetFragment extends Fragment {

    private static final String TAG = "ADDPET";
    private final String title = "เพิ่มข้อมูลสัตว์เลี้ยง";

    private Toolbar zToolBar;
    private RadioGroup zType;
    private RadioGroup zSex;
    private Spinner zYear;
    private Spinner zMonth;
    private Spinner zDay;
    private TextView zPetname;
    private Button zAddBtn;
    private ImageView zImageViewPet;
    private ImageView zImageViewClick;
    private Bitmap selectedImage;
    private Uri uriImage;
    private ProgressDialog zLoadingDialog;

    private ArrayList<Integer> zYearData = new ArrayList<Integer>();
    private ArrayList<Integer> zMonthData = new ArrayList<Integer>();
    private ArrayList<Integer> zDayData = new ArrayList<Integer>();

    private User user;
    private Pet pet;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_addpet, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivity.onFragmentChanged(TAG);

        zLoadingDialog = new ProgressDialog(getActivity());
        zLoadingDialog.setCancelable(false);
        zLoadingDialog.setCanceledOnTouchOutside(false);

        user = User.getUserInstance();
        pet = Pet.getPetInstance();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        addpetFragmentElements();
        createMenu();
        createSpinner();
        createSpinnerAdapter();
        imageController();
        initAddBtn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            try {
                uriImage = data.getData();
                InputStream imageStream = getActivity().getContentResolver().openInputStream(uriImage);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = getResizedBitmap(selectedImage, 400);// 400 is for example, replace with desired size
                Glide.with(getActivity()).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(zImageViewPet);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void imageController() {
        zImageViewClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "กรุณาเลือกรูปภาพสัตว์เลี้ยงของคุณ"), 1);
            }
        });
    }

    private void displaySuccessDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("เพิ่มข้อมูลสัตว์เลี้ยง")
                .setMessage("เพิ่มข้อมูลสำเร็จ.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_view, new HomeFragment())
                                .commit();
                    }
                })
                .show();
    }

    private void displayFailureDialog(String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("เกิดข้อผิดพลาด")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void addpetFragmentElements() {
        zToolBar = getView().findViewById(R.id.toolbar);
        zType = getView().findViewById(R.id.frg_addpet_typegroup);
        zSex = getView().findViewById(R.id.frg_addpet_sexgroup);
        zYear = getView().findViewById(R.id.frg_addpet_year);
        zMonth = getView().findViewById(R.id.frg_addpet_month);
        zDay = getView().findViewById(R.id.frg_addpet_day);
        zPetname = getView().findViewById(R.id.frg_addpet_petname);
        zAddBtn = getView().findViewById(R.id.frg_addpet_addbtn);
        zImageViewPet = getView().findViewById(R.id.frg_addpet_image);
        zImageViewClick = getView().findViewById(R.id.frg_addpet_image_click);
    }

    private void createSpinner() {
        for (int year = 0; year <= 15; year++) {
            zYearData.add(year);
        }

        for (int month = 0; month <= 12; month++) {
            zMonthData.add(month);
        }

        for (int day = 0; day <= 15; day++) {
            zDayData.add(day);
        }
    }

    private void createSpinnerAdapter() {
        ArrayAdapter<Integer> zAdapterYear = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_dropdown_item_1line, zYearData);
        ArrayAdapter<Integer> zAdapterMonth = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_dropdown_item_1line, zMonthData);
        ArrayAdapter<Integer> zAdapterDay = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_dropdown_item_1line, zDayData);
        zYear.setAdapter(zAdapterYear);
        zMonth.setAdapter(zAdapterMonth);
        zDay.setAdapter(zAdapterDay);
    }

    private void createMenu() {
        Log.d(TAG, "create menu");
        Objects.requireNonNull(getActivity()).setActionBar(zToolBar);
        zToolBar.setTitle(title);
        zToolBar.setNavigationIcon(R.drawable.ic_arrow_back);
        zToolBar.setNavigationContentDescription("Back");
        zToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void initAddBtn() {
        zAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zLoadingDialog.setMessage("กำลังเพิ่มข้อมูล...");
                zLoadingDialog.show();

                int typeId = zType.getCheckedRadioButtonId();
                int sexId = zSex.getCheckedRadioButtonId();
                RadioButton zTypeRadio = getView().findViewById(typeId);
                RadioButton zSexRadio = getView().findViewById(sexId);

                final String type = zTypeRadio.getText().toString();
                final String sex = zSexRadio.getText().toString();
                final String petname = zPetname.getText().toString();
                final String year = zYear.getSelectedItem().toString();
                final String month = zMonth.getSelectedItem().toString();
                final String day = zDay.getSelectedItem().toString();

                if (type.isEmpty() || sex.isEmpty() || petname.isEmpty() || year.isEmpty() || month.isEmpty() || day.isEmpty()) {
                    Toast.makeText(getActivity(),
                            "กรุณาใส่ข้อมูลให้ครบถ้วน",
                            Toast.LENGTH_LONG).show();
                    zLoadingDialog.dismiss();
                } else if (zImageViewPet.getDrawable() == null || uriImage == null) {
                    Toast.makeText(getActivity(),
                            "กรุณาเลือกรูปสัตว์เลี้ยงของคุณ",
                            Toast.LENGTH_LONG).show();
                    zLoadingDialog.dismiss();
                } else if (year.equals("0") && month.equals("0") && day.equals("0")) {
                    Toast.makeText(getActivity(),
                            "กรุณาเลือกอายุสัตว์ลี้ยงของคุณ",
                            Toast.LENGTH_LONG).show();
                    zLoadingDialog.dismiss();
                } else {
                    final DateFormat dateFormatDoc = new SimpleDateFormat("ddMMyyyyHHmmss");
                    final Date date = new Date();

                    pet.setPetkey(dateFormatDoc.format(date));
                    pet.setPetname(petname);
                    pet.setPetday(day);
                    pet.setPetmonth(month);
                    pet.setPetyear(year);
                    pet.setPetsex(sex);
                    pet.setPettype(type);
                    pet.setPetownerUid(user.getUid());

                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] dataImage = baos.toByteArray();

                    final StorageReference uploadTask;
                    uploadTask = storageReference.child("images/pets/" + user.getUid() + "/" + pet.getPetkey());

                    pet.setPetimage(uploadTask.getPath());

                    uploadTask.putBytes(dataImage, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "upload image : success");
                            firebaseFirestore.collection("pet").document(dateFormatDoc.format(date))
                                    .set(pet).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    zLoadingDialog.dismiss();
                                    displaySuccessDialog();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    zLoadingDialog.dismiss();
                                    displayFailureDialog(e.getMessage());
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "upload image : failed");
                            zLoadingDialog.dismiss();
                            displayFailureDialog("ขออภัย, อัพโหลดรูปภาพไม่สำเร็จ");
                        }
                    });

                }
            }
        });
    }
}

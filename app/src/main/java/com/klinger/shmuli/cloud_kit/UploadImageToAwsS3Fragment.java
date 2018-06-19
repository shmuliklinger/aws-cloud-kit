package com.klinger.shmuli.cloud_kit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UploadImageToAwsS3Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UploadImageToAwsS3Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadImageToAwsS3Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;
    Button takePicButton = null;
    Button uploadPicButton = null;
    ImageView imageView = null;
    String imageFilePath = null;
    public UploadImageToAwsS3Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CamraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadImageToAwsS3Fragment newInstance(String param1, String param2) {
        UploadImageToAwsS3Fragment fragment = new UploadImageToAwsS3Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_upload_image_to_aws_s3, container, false);

        takePicButton = (Button) rootView.findViewById(R.id.button_take_pic);
        uploadPicButton = (Button) rootView.findViewById(R.id.button_upload);
        imageView = (ImageView) rootView.findViewById(R.id.image_before);



        uploadPicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = "public/"+ imageFilePath.substring(imageFilePath.lastIndexOf("/")+1);

                if(imageFilePath != null) {
                    uploadWithTransferUtility(fileName, imageFilePath);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Pic is null", Toast.LENGTH_SHORT).show();
                }
            }
        });



        takePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
                    //Create a file to store the image
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {

                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),"com.klinger.shmuli.cloud_kit.provider", photoFile);
                        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(pictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    }

            }}
        });
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(imageFilePath).into(imageView);
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void uploadWithTransferUtility(String name, String path) {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getActivity().getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();

        final TransferObserver uploadObserver =
                transferUtility.upload(name,new File(path));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Log.d("YourActivity", "Completed");
                    String fileLocation = uploadObserver.getBucket() + "/" + uploadObserver.getKey();
                    Toast.makeText(getActivity().getApplicationContext(), "Pic uploaded to: " + fileLocation, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("YourActivity", ex.getMessage());
                Toast.makeText(getActivity().getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            Log.d("YourActivity", "Completed");
            String fileLocation = uploadObserver.getBucket() + "/" + uploadObserver.getKey();
            Toast.makeText(getActivity().getApplicationContext(), "Pic uploaded to: " + fileLocation, Toast.LENGTH_LONG).show();
        }

        Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }
}

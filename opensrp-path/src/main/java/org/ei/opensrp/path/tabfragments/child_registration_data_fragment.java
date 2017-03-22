package org.ei.opensrp.path.tabfragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.path.R;
import org.ei.opensrp.path.activity.ChildDetailTabbedActivity;
import org.ei.opensrp.path.viewComponents.WidgetFactory;
import org.ei.opensrp.repository.DetailsRepository;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import util.DateUtils;
import util.Utils;


public class child_registration_data_fragment extends Fragment {
    public CommonPersonObjectClient childDetails;
    public Map<String, String> Detailsmap;
    public child_registration_data_fragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getArguments();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (this.getArguments() != null) {
            Serializable serializable = getArguments().getSerializable(ChildDetailTabbedActivity.EXTRA_CHILD_DETAILS);
            if (serializable != null && serializable instanceof CommonPersonObjectClient) {
                childDetails = (CommonPersonObjectClient) serializable;
            }
        }
        View fragmentview = inflater.inflate(R.layout.child_registration_data_fragment, container, false);
        LinearLayout layout = (LinearLayout)fragmentview.findViewById(R.id.rowholder);

        DetailsRepository detailsRepository = org.ei.opensrp.Context.getInstance().detailsRepository();
        Detailsmap  = detailsRepository.getAllDetailsForClient(childDetails.entityId());
//        Detailsmap = childDetails.getColumnmaps();
        WidgetFactory wd = new WidgetFactory();


        layout.addView(wd.createTableRow(inflater,container,"Child's home health facility", Utils.getValue(childDetails.getColumnmaps(),"Home_Facility",false)));
        layout.addView(wd.createTableRow(inflater,container,"Child's ZEIR ID",Utils.getValue(childDetails.getColumnmaps(),"zeir_id",false)));
        layout.addView(wd.createTableRow(inflater,container,"Child's register card number",Utils.getValue(Detailsmap,"Child_Register_Card_Number",false)));
        layout.addView(wd.createTableRow(inflater,container,"Child's birth certificate number",Utils.getValue(Detailsmap,"Child_Birth_Certificate",false)));
        layout.addView(wd.createTableRow(inflater,container,"First Name",Utils.getValue(childDetails.getColumnmaps(),"first_name",true)));
        layout.addView(wd.createTableRow(inflater,container,"Last Name",Utils.getValue(childDetails.getColumnmaps(),"last_name",true)));
        layout.addView(wd.createTableRow(inflater,container,"Sex",Utils.getValue(childDetails.getColumnmaps(),"gender",true)));
        layout.addView(wd.createTableRow(inflater,container,"Child's DOB",ChildDetailTabbedActivity.DATE_FORMAT.format( new DateTime(Utils.getValue(childDetails.getColumnmaps(),"dob",true)).toDate())));


        String formattedAge = "";
        String dobString = Utils.getValue(childDetails.getColumnmaps(), "dob", false);
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            Date dob = dateTime.toDate();
            long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

            if (timeDiff >= 0) {
                formattedAge = DateUtils.getDuration(timeDiff);
            }
        }


        layout.addView(wd.createTableRow(inflater,container,"Age",formattedAge));


        layout.addView(wd.createTableRow(inflater,container,"Date First Seen",Utils.getValue(Detailsmap,"First_Health_Facility_Contact",true)));
        layout.addView(wd.createTableRow(inflater,container,"Birth Weight",Utils.getValue(childDetails.getColumnmaps(),"Birth_Weight",true)));

        layout.addView(wd.createTableRow(inflater,container,"Mother/guardian first name",Utils.getValue(childDetails.getColumnmaps(),"mother_first_name",true)));
        layout.addView(wd.createTableRow(inflater,container,"Mother/guardian last name",Utils.getValue(childDetails.getColumnmaps(),"mother_last_name",true)));
        layout.addView(wd.createTableRow(inflater,container,"Mother/guardian DOB",Utils.getValue(Detailsmap,"Mother_Guardian_Date_Birth",true)));

        layout.addView(wd.createTableRow(inflater,container,"Mother/Guardian NRC",Utils.getValue(Detailsmap,"Mother_Guardian_NRC",true)));
        layout.addView(wd.createTableRow(inflater,container,"Mother/guardian phone number",Utils.getValue(Detailsmap,"Mother_Guardian_Number",true)));
        layout.addView(wd.createTableRow(inflater,container,"Father/guardian name",Utils.getValue(Detailsmap,"Father_Guardian_Name",true)));
        layout.addView(wd.createTableRow(inflater,container,"Father/guardian NRC",Utils.getValue(Detailsmap,"Father_Guardian_NRC",true)));
        layout.addView(wd.createTableRow(inflater,container,"Place of birth",Utils.getValue(Detailsmap,"Place_Birth",true)));
        layout.addView(wd.createTableRow(inflater,container,"Health facility the child was born in",Utils.getValue(Detailsmap,"Birth_Facility_Name",true)));
        layout.addView(wd.createTableRow(inflater,container,"Child's residential area",Utils.getValue(Detailsmap,"Residential_Area",true)));
        layout.addView(wd.createTableRow(inflater,container,"Other residential area",Utils.getValue(Detailsmap,"Residential_Area_Other",true)));
        layout.addView(wd.createTableRow(inflater,container,"Home address",Utils.getValue(Detailsmap,"Residential_Address",true)));

        layout.addView(wd.createTableRow(inflater,container,"Landmark",Utils.getValue(Detailsmap,"Physical_Landmark",true)));
        layout.addView(wd.createTableRow(inflater,container,"CHW name",Utils.getValue(Detailsmap,"CHW_Name",true)));
        layout.addView(wd.createTableRow(inflater,container,"CHW phone number",Utils.getValue(Detailsmap,"CHW_Phone_Number",true)));
        layout.addView(wd.createTableRow(inflater,container,"HIV exposure",Utils.getValue(Detailsmap,"PMTCT_Status",true)));


//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));
//        layout.addView(createTableRow(inflater,container,"Catchment Area","Linda"));




        // Inflate the layout for this fragment
        return fragmentview;
    }





}

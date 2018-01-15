package org.ei.opensrp.madagascar.HHmember;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import static org.ei.opensrp.util.StringUtil.humanize;

import org.ei.opensrp.commonregistry.AllCommonsRepository;
import org.ei.opensrp.commonregistry.CommonPersonObject;
import org.ei.opensrp.commonregistry.CommonPersonObjectClient;
import org.ei.opensrp.commonregistry.CommonPersonObjectController;
import org.ei.opensrp.cursoradapter.SmartRegisterCLientsProviderForCursorAdapter;
import org.ei.opensrp.madagascar.R;
import org.ei.opensrp.service.AlertService;
import org.ei.opensrp.view.contract.SmartRegisterClient;
import org.ei.opensrp.view.contract.SmartRegisterClients;
import org.ei.opensrp.view.dialog.FilterOption;
import org.ei.opensrp.view.dialog.ServiceModeOption;
import org.ei.opensrp.view.dialog.SortOption;
import org.ei.opensrp.view.viewHolder.OnClickFormLauncher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by Dimas Ciputra on 2/16/15.
 */
public class HHmemberClientsProvider implements SmartRegisterCLientsProviderForCursorAdapter {
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private Drawable iconPencilDrawable;
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;

    /*
    *  private static final String TAG = GiziSmartClientsProvider.class.getSimpleName();
    private final LayoutInflater inflater;
    private final Context context;
    private final View.OnClickListener onClickListener;
    private Drawable iconPencilDrawable;
    private final int txtColorBlack;
    private final AbsListView.LayoutParams clientViewLayoutParams;

    protected CommonPersonObjectController controller;
    */
    AlertService alertService;
    public HHmemberClientsProvider(Context context,
                                   View.OnClickListener onClickListener,
                                   AlertService alertService) {
        this.onClickListener = onClickListener;
//        this.controller = controller;
        this.context = context;
        this.alertService = alertService;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        clientViewLayoutParams = new AbsListView.LayoutParams(MATCH_PARENT,
                (int) context.getResources().getDimension(org.ei.opensrp.R.dimen.list_item_height));
        txtColorBlack = context.getResources().getColor(org.ei.opensrp.R.color.text_black);

    }

    @Override
    public void getView(SmartRegisterClient smartRegisterClient, View convertView) {
        ViewHolder viewHolder;
        if(convertView.getTag() == null || !(convertView.getTag() instanceof  ViewHolder)) {
            viewHolder = new ViewHolder();
            viewHolder.profilelayout =  (LinearLayout)convertView.findViewById(R.id.profile_info_layout);
            viewHolder.HH_name = (TextView)convertView.findViewById(R.id.HH_name);
            viewHolder.husband_name = (TextView)convertView.findViewById(R.id.txt_husband_name);
            viewHolder.village_name = (TextView)convertView.findViewById(R.id.txt_village_name);
            viewHolder.wife_age = (TextView)convertView.findViewById(R.id.wife_age);

            /*education
    pregnant fp menopause
            * */
            viewHolder.educations = (TextView)convertView.findViewById(R.id.education);
           // viewHolder.education = (TextView)convertView.findViewById(R.id.education);
            viewHolder.professions = (TextView)convertView.findViewById(R.id.profession);
            viewHolder.maritals = (TextView)convertView.findViewById(R.id.marital);

            viewHolder.pregnants = (TextView)convertView.findViewById(R.id.pregnant);
            viewHolder.fps = (TextView)convertView.findViewById(R.id.fp);
            viewHolder.menopauses = (TextView)convertView.findViewById(R.id.menopause);

            viewHolder.weights =(TextView)convertView.findViewById(R.id.weight);
            viewHolder.heights = (TextView)convertView.findViewById(R.id.height);
            viewHolder.muacs = (TextView)convertView.findViewById(R.id.muac);

            viewHolder.profilepic =(ImageView)convertView.findViewById(R.id.img_profile);
            viewHolder.follow_up = (ImageButton)convertView.findViewById(R.id.btn_edit);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.follow_up.setOnClickListener(onClickListener);
        viewHolder.follow_up.setTag(smartRegisterClient);
        viewHolder.profilelayout.setOnClickListener(onClickListener);
        viewHolder.profilelayout.setTag(smartRegisterClient);
        CommonPersonObjectClient pc = (CommonPersonObjectClient) smartRegisterClient;
        if (iconPencilDrawable == null) {
            iconPencilDrawable = context.getResources().getDrawable(R.drawable.ic_pencil);
        }
        viewHolder.follow_up.setImageDrawable(iconPencilDrawable);
        viewHolder.follow_up.setOnClickListener(onClickListener);

        final ImageView kiview = (ImageView)convertView.findViewById(R.id.img_profile);
     /*   if(pc.getDetails().get("profilepic") !=null) {
            HHmemberDetailActivity.setImagetoHolderFromUri((Activity) context, pc.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);
            kiview.setTag(smartRegisterClient);

        }

        else {*/

            if (pc.getColumnmaps().get("Sex") != null) {
                if (pc.getColumnmaps().get("Sex").equalsIgnoreCase("Female")) {
                    viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.woman_placeholder));
                    if(pc.getDetails().get("profilepic") !=null) {
                        HHmemberDetailActivity.setImagetoHolderFromUri((Activity) context, pc.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);
                        kiview.setTag(smartRegisterClient);

                    }
                }
                else if(pc.getColumnmaps().get("Sex").equalsIgnoreCase("Male")) {
                    viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.household_profile));
                    if(pc.getDetails().get("profilepic") !=null) {
                        HHmemberDetailActivity.setImagetoHolderFromUri((Activity) context, pc.getDetails().get("profilepic"), kiview, R.mipmap.household_profile);
                        kiview.setTag(smartRegisterClient);

                    }
                }
            }
            else{
                viewHolder.profilepic.setImageDrawable(context.getResources().getDrawable(R.mipmap.woman_placeholder));

            }




        viewHolder.HH_name.setText( pc.getColumnmaps().get("Name_family_member") !=null?pc.getColumnmaps().get("Name_family_member"):"");
        viewHolder.husband_name.setText(pc.getColumnmaps().get("Ethnic_Group")!= null?pc.getColumnmaps().get("Ethnic_Group"):"");
       // viewHolder.village_name.setText(pc.getColumnmaps().get("Education")!= null?pc.getColumnmaps().get("Education"):"");

        if( pc.getDetails().get("HHId") != null) {
            String kiid = pc.getDetails().get("HHId");
            AllCommonsRepository iburep = org.ei.opensrp.Context.getInstance().allCommonsRepositoryobjects("HH");
            final CommonPersonObject ibuparent = iburep.findByCaseID(kiid);
            viewHolder.village_name.setText(ibuparent.getDetails().get("Village") != null ? ibuparent.getDetails().get("Village") : "");
        }

        String value_education = pc.getColumnmaps().get("Education") != null ? pc.getColumnmaps().get("Education") : "";
        String value_profession = pc.getDetails().get("Profession_choices") !=null?humanize(pc.getDetails().get("Profession_choices")) :"";
        String value_maritals = pc.getDetails().get("Marital_Status") !=null?pc.getDetails().get("Marital_Status"):"";

        String educate = value_compare(value_education,"None","EPP","CEG","Lycee","University","-","-","-",
                context.getString(R.string.None),context.getString(R.string.EPP),
                context.getString(R.string.CEG),context.getString(R.string.Lycee),context.getString(R.string.University),null,null,null);


        String profession = value_compare(value_profession,"Farmer","Shop_owner","Fisher","Teacher","Government","Profession_Other","student","Profession_NA",
                context.getString(R.string.Farmer),context.getString(R.string.Shop_owner),
                context.getString(R.string.Fisher),context.getString(R.string.Teacher),context.getString(R.string.Government)
                ,context.getString(R.string.Profession_Other),context.getString(R.string.student),context.getString(R.string.Profession_NA));


        String maried = value_compare(value_maritals,"Single","Married","Fisher","Divorced","Widowed","-","-","-",
                context.getString(R.string.Single),context.getString(R.string.Married),
                context.getString(R.string.Fisher),context.getString(R.string.Divorced),context.getString(R.string.Widowed)
                ,null, null, null);

        viewHolder.educations.setText(context.getString(R.string.education) + ": " +educate );
       // viewHolder.education.setText(context.getString(R.string.education) + " " + pc.getColumnmaps().get("Education") !=null?pc.getColumnmaps().get("Education"):"");
        viewHolder.professions.setText(context.getString(R.string.profession) + ": " + value_profession);
        viewHolder.maritals.setText(context.getString(R.string.marital) + ": " + maried);


        viewHolder.pregnants.setText("");
        if (pc.getColumnmaps().get("Sex") != null) {
            if (pc.getColumnmaps().get("Sex").equalsIgnoreCase("Female")) {
                viewHolder.pregnants.setText(
                        pc.getDetails().get("Pregnant_now") != null ?
                                context.getString(R.string.pregnancy) + ": " + pc.getDetails().get("Pregnant_now") :
                                pc.getDetails().get("Contraception_Type") != null ?
                                        context.getString(R.string.contraseption) + ": " + pc.getDetails().get("Contraception_Type") :
                                        pc.getDetails().get("Menopause") != null ?
                                                context.getString(R.string.menopause) + ": " + pc.getDetails().get("Menopause")
                                                : "");
            }
        }
        else
        {
            viewHolder.pregnants.setText("");
        }
    viewHolder.weights.setText(pc.getDetails().get("childWeight") !=null?context.getString(R.string.str_weight)+" "+pc.getDetails().get("childWeight"):"");
        viewHolder.heights.setText(pc.getDetails().get("childHeight") !=null?context.getString(R.string.height)+" "+pc.getDetails().get("childHeight"):"");
        viewHolder.muacs.setText(pc.getDetails().get("anthropmetryUpperArm") !=null?context.getString(R.string.muac)+" "+pc.getDetails().get("anthropmetryUpperArm"):"");

        //}

        //distance to nearest
        convertView.setLayoutParams(clientViewLayoutParams);
      //  return convertView;
    }

    private String value_compare(String value_education,String value1,String value2,String value3,String value4,String value5,String value6,String value7,String value8,
                                 String a, String b, String c, String d, String e, String f, String g, String h) {

        String as =value_education.equalsIgnoreCase(value1) ? a:
                value_education.equalsIgnoreCase(value2)?b:
                        value_education.equalsIgnoreCase(value3)?c:
                                value_education.equalsIgnoreCase(value4)?d:
                                        value_education.equalsIgnoreCase(value5)?e:
                                                value_education.equalsIgnoreCase(value6)?f:
                                                        value_education.equalsIgnoreCase(value7)?g:
                                                                value_education.equalsIgnoreCase(value8)?h:
                                                "";
        return as;
    }

    CommonPersonObjectController householdelcocontroller;
    

    //    @Override
    public SmartRegisterClients getClients() {
        return controller.getClients();
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                              FilterOption searchFilter, SortOption sortOption) {
        return getClients().applyFilter(villageFilter, serviceModeOption, searchFilter, sortOption);
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {
        // do nothing.
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public View inflatelayoutForCursorAdapter() {
        View View = inflater().inflate(R.layout.smart_register_hhmember_client, null);
        return View;
    }

    class ViewHolder {

        TextView wife_name ;
        TextView husband_name ;
        TextView village_name;
        TextView wife_age;
        LinearLayout profilelayout;
        ImageView profilepic;
         TextView HH_name;
        ImageButton follow_up;
        TextView professions;
        TextView maritals;
        TextView educations;
        TextView pregnants;
        TextView fps;
        TextView menopauses;

        TextView weights;
        TextView heights;
        TextView muacs;

    }


}
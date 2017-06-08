package util.KMS;

/**
 * Created by Iq on 27/05/16.
 */
public class KmsCalc {

    public int monthAges(String lastVisitDate,String currentDate){

            int tahun = Integer.parseInt(currentDate.substring(0, 4)) - Integer.parseInt(lastVisitDate.substring(0, 4));
            int bulan = Integer.parseInt(currentDate.substring(5, 7)) - Integer.parseInt(lastVisitDate.substring(5, 7));
            int hari = Integer.parseInt(currentDate.substring(8)) - Integer.parseInt(lastVisitDate.substring(8));
            return (tahun * 12 + bulan + (int) (hari / 30));

    }

    public String cek2T(KmsPerson bayi){
        boolean status = true;
       // ////System.out.println("check 2T");
        String measureDate[] = {bayi.getLastVisitDate(),bayi.getSecondLastVisitDate()};
        double weight[] = {bayi.getWeight(),bayi.getPreviousWeight()};
        status = status && (cekWeightStatus(bayi.isMale(), bayi.getDateOfBirth(), measureDate, weight).toLowerCase().equals("not gaining weight"));
        ////System.out.println("status 1: "+status+", weight: "+weight[0]+", "+weight[1]);
        String measureDate2[] = {bayi.getSecondLastVisitDate(),bayi.getThirdLastVisitDate()};
        double weight2[] = {bayi.getPreviousWeight(),bayi.getSecondLastWeight()};
        status = status && (cekWeightStatus(bayi.isMale(), bayi.getDateOfBirth(), measureDate2, weight2).toLowerCase().equals("not gaining weight"));
        ////System.out.println("status 2: "+status+", weight: "+weight2[0]+", "+weight2[1]);
        bayi.Tidak2Kali = status;
        return (bayi.Tidak2Kali ? "Yes":"No");
    }



    public String cekWeightStatus(KmsPerson bayi){
        ////System.out.println("check weight status");
        String measureDate[] = {bayi.getLastVisitDate(),bayi.getSecondLastVisitDate()};
        double weight[] = {bayi.getWeight(),bayi.getPreviousWeight()};
        bayi.StatusBeratBadan = cekWeightStatus(bayi.isMale(),bayi.getDateOfBirth(),measureDate,weight);

        return  bayi.StatusBeratBadan;
    }

    public String cekWeightStatus(boolean isMale, String dateOfBirth, String measureDate[], double weight[]){
        if( measureDate.equals("0") || measureDate[0].equals("") || measureDate[1].equals(""))
            return "New";
        else {
            int age = monthAges(dateOfBirth, measureDate[0]);
            int range = monthAges(measureDate[1], measureDate[0]);
            int stagnanIndicator = (isMale ? 12 : 11);
            int index = age > stagnanIndicator ? stagnanIndicator : age;

            return range > 1
                    ? "Not attending previous visit"
                    : ((weight[0] - weight[1] + 0.000000000000004) * 1000)  >= KmsConstants.maleWeightUpIndicator[index]
                        ? "Weight Increase"
                        : "Not gaining weight";
        }
    }

    public String cekBGM(KmsPerson bayi){
        bayi.BGM = bayi.isMale()
                ? KmsConstants.maleBGM[bayi.getAge()]>bayi.getWeight()
                : KmsConstants.femaleBGM[bayi.getAge()]>bayi.getWeight();
        return ""+(bayi.BGM ? "Yes":"No");
    }

    public String cekBawahKuning(KmsPerson bayi){
        int umur = bayi.getAge();
        ////System.out.println(KmsConstants.femaleGarisKuning[bayi.getAge()][0]);
        ////System.out.println(bayi.getWeight());
        ////System.out.println(KmsConstants.femaleGarisKuning[bayi.getAge()][1]);
        bayi.GarisKuning = bayi.isMale()
                ? ((KmsConstants.maleGarisKuning[bayi.getAge()][0]<=bayi.getWeight())
                && (bayi.getWeight()<=KmsConstants.maleGarisKuning[bayi.getAge()][1]))
                : ((KmsConstants.femaleGarisKuning[bayi.getAge()][0]<=bayi.getWeight())
                && (bayi.getWeight()<=KmsConstants.femaleGarisKuning[bayi.getAge()][1]))
        ;
        ////System.out.println(bayi.GarisKuning ? "yes":"no");
        return ""+(bayi.GarisKuning ? "Yes":"No");
    }

}

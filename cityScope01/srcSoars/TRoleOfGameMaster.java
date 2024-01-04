

import jp.soars.core.TObject;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;


/**
 * スポットの役割を定義するクラス
 */
public class TRoleOfGameMaster extends TRole {

    private static final String RULE_NAME_OF_UPDATEWORLD = "updateworld";

    public TRoleOfGameMaster(TSpot spot) {
        super(ERoleName.GameMaster, spot);

        new TRuleOfUpdateWorld("UpdateWorld",this).setTimeAndStage(0,0,0,EStage.UpdateWorld);
        // その他のメソッドや機能をここに追加


    }

}
import jp.soars.core.*;

import java.util.Map;

public class TRuleOfUpdateWorld extends TAgentRule {



    public TRuleOfUpdateWorld(String name, TRole owner) {
        // 親クラスのコンストラクタを呼び出す．
        super(name, owner);
        System.out.print("update");
    }

    /**
     * ルールを実行する．
     * @param currentTime 現在時刻
     * @param currentStage 現在ステージ
     * @param spotManager スポット管理
     * @param agentManager エージェント管理
     * @param globalSharedVariables グローバル共有変数集合
     */
    @Override
    public final void doIt(TTime currentTime, Enum<?> currentStage, TSpotManager spotManager,
                           TAgentManager agentManager, Map<String, Object> globalSharedVariables) {

        // 処理の内容
        System.out.print("UpdateWorld");
    }



}

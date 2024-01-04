import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;

import jp.soars.core.TAgent;
import jp.soars.core.TAgentManager;
import jp.soars.core.TRuleExecutor;
import jp.soars.core.TSOARSBuilder;
import jp.soars.core.TSpot;
import jp.soars.core.TSpotManager;
import jp.soars.core.enums.ERuleDebugMode;
import jp.soars.utils.random.ICRandom;

/**
 * メインクラス
 * @author nagakane
 */
public class TMain {

    public static void main(String[] args) throws IOException {
        // *************************************************************************************************************
        // TSOARSBuilderの必須設定項目
        //   - simulationStart:シミュレーション開始時刻
        //   - simulationEnd:シミュレーション終了時刻
        //   - tick:1ステップの時間間隔
        //   - stages:使用するステージリスト(実行順)
        //   - agentTypes:使用するエージェントタイプ集合
        //   - spotTypes:使用するスポットタイプ集合
        // *************************************************************************************************************

        String simulationStart = "0/00:00:00";
        String simulationEnd = "1/00:00:00";
        String tick = "1:00:00";
        List<Enum<?>> stages = List.of(EStage.UpdateWorld,EStage.AgentMoving);
        Set<Enum<?>> agentTypes = new HashSet<>();
        Collections.addAll(agentTypes, EAgentType.values());
        Set<Enum<?>> spotTypes = new HashSet<>();
        Collections.addAll(spotTypes, ESpotType.values());
        TSOARSBuilder builder = new TSOARSBuilder(simulationStart, simulationEnd, tick, stages, agentTypes, spotTypes);

        // *************************************************************************************************************
        // TSOARSBuilderの任意設定項目
        // *************************************************************************************************************

        // マスター乱数発生器のシード値設定
        long seed = 0L;
        builder.setRandomSeed(seed);

        // ルールログとランタイムログの出力設定
        String pathOfLogDir = "C:/Users/関颯太/IdeaProjects/tora233824/cityScope01/srcSoars/";
        builder.setRuleLoggingEnabled(pathOfLogDir+ "rule_log.csv");
        builder.setRuntimeLoggingEnabled(pathOfLogDir +"runtime_log.csv");

        // ルールログのデバッグ情報出力設定
        builder.setRuleDebugMode(ERuleDebugMode.LOCAL);

        // *************************************************************************************************************
        // TSOARSBuilderでシミュレーションに必要なインスタンスの作成と取得
        // *************************************************************************************************************

        builder.build();
        TRuleExecutor ruleExecutor = builder.getRuleExecutor();
        TAgentManager agentManager = builder.getAgentManager();
        TSpotManager spotManager = builder.getSpotManager();
        ICRandom random = builder.getRandom();
        Map<String, Object> globalSharedVariableSet = builder.getGlobalSharedVariableSet();



        // *************************************************************************************************************
        // Requestダミーデータの生成
        // *************************************************************************************************************

        CreateRequestTable dbInitializer = new CreateRequestTable(pathOfLogDir  + "database.db",pathOfLogDir+"dummy_data.csv");
        dbInitializer.initializeTableFromCSV();


        // *************************************************************************************************************
        // スポット作成
        //   - Home:Home1, Home2, Home3
        //   - Company:Company
        // *************************************************************************************************************


        //Gamemasterを生成
        TSpot gamemaster = spotManager.createSpot(ESpotType.GameMaster);
        TRoleOfGameMaster roleOfGameMaster = new TRoleOfGameMaster(gamemaster);
        gamemaster.activateRole(ERoleName.GameMaster);


        // homeスポットを3個生成
        int noOfHomes = 50;

        List<TSpot> homes = spotManager.createSpots(ESpotType.Home, noOfHomes);

        for (int i = 0; i < noOfHomes; i++) {
            // ランダムな座標を生成
            int x = random.nextInt(10); // 0から9までのランダムな整数
            int y = random.nextInt(10); // 0から9までのランダムな整数

            // 新しいスポットを作成し、RoleOfSpotを割り当てる
            TSpot home = homes.get(i);
            TRoleOfSpot roleOfSpot = new TRoleOfSpot(home,x, y);
            home.activateRole(ERoleName.Spot);
        }



        int noOfcompany = 3;
        List<TSpot> companies = spotManager.createSpots(ESpotType.Company, noOfcompany);
        for (int i = 0; i < noOfcompany; i++) {
            // ランダムな座標を生成
            int x = random.nextInt(10); // 0から9までのランダムな整数
            int y = random.nextInt(10); // 0から9までのランダムな整数

            // 新しいスポットを作成し、RoleOfSpotを割り当てる
            TSpot company = companies.get(i);
            TRoleOfSpot roleOfSpot = new TRoleOfSpot(company,x, y);
            company.activateRole(ERoleName.Spot);
        }

        // *************************************************************************************************************
        // エージェント作成
        //   - Father:Father1, Father2, Father3
        //     - 初期スポット:Home
        //     - 役割:父親役割
        // *************************************************************************************************************

        int noOfhuman = noOfHomes; // 父親の数は家の数と同じ．
        List<TAgent> humans = agentManager.createAgents(EAgentType.Human, noOfhuman);
        for (int i = 0; i < noOfhuman; ++i) {
            TAgent human = humans.get(i); // i番目の父親エージェント
            TSpot home = homes.get(i); // i番目の父親エージェントの自宅
            human.initializeCurrentSpot(home); // 初期スポットを自宅に設定

            new TRoleOfWorker(human, home, companies.get(random.nextInt(3))); // 父親役割を作成
            human.activateRole(ERoleName.Worker); // 父親役割をアクティブ化
        }










        // *************************************************************************************************************
        // 独自に作成するログ用のPrintWriter
        //   - スポットログ:各時刻での各エージェントの現在位置ログ
        // *************************************************************************************************************

        // スポットログ用PrintWriter
        PrintWriter spotLogPW = new PrintWriter(new BufferedWriter(new FileWriter(pathOfLogDir + File.separator + "spot_log.csv")));
        // スポットログのカラム名出力
        spotLogPW.print("CurrentTime,SpotName,X,Y,population");
//        for (TAgent father : fathers) {
//            spotLogPW.print(',');
//            spotLogPW.print(father.getName());
//        }

        spotLogPW.println();

        // *************************************************************************************************************
        // シミュレーションのメインループ
        // *************************************************************************************************************

        // 1ステップ分のルールを実行 (ruleExecutor.executeStage()で1ステージ毎に実行することもできる)
        // 実行された場合:true，実行されなかった(終了時刻)場合は:falseが帰ってくるため，while文で回すことができる．
        while (ruleExecutor.executeStep()) {
            // 標準出力に現在時刻を表示する
            System.out.println(ruleExecutor.getCurrentTime());

            // スポットログ出力
//            spotLogPW.print(ruleExecutor.getCurrentTime());
//            for (TAgent father : fathers) {
//                spotLogPW.print(',');
//                spotLogPW.print(father.getCurrentSpotName());
//            }

//            for (TAgent father : fathers) {
//                TRoleOfSpot roleOfSpot = (TRoleOfSpot) father.getCurrentSpot().getRole(ERoleName.Spot);
//                spotLogPW.print(ruleExecutor.getCurrentTime());
//                spotLogPW.print(',');
//                spotLogPW.print(father.getName());
//                spotLogPW.print(',');
//                spotLogPW.print(father.getCurrentSpotName());
//                spotLogPW.print(',');
//                spotLogPW.print(roleOfSpot.getX());
//                spotLogPW.print(',');
//                spotLogPW.println(roleOfSpot.getY());
//
//            }


            List<TSpot> HomeSpots = spotManager.getSpots(ESpotType.Home);
            List<TSpot> CompanySpots = spotManager.getSpots(ESpotType.Company);

            List<TSpot> allSpots = new ArrayList<>();
            allSpots.addAll(HomeSpots);
            allSpots.addAll(CompanySpots);


            for (TSpot spot : allSpots) {
                TRoleOfSpot roleOfSpot = (TRoleOfSpot) spot.getRole(ERoleName.Spot);
                spotLogPW.print(ruleExecutor.getCurrentTime());
                spotLogPW.print(',');
                spotLogPW.print(spot.getName());
                spotLogPW.print(',');
                spotLogPW.print(roleOfSpot.getX());
                spotLogPW.print(',');
                spotLogPW.print(roleOfSpot.getY());
                spotLogPW.print(',');
                spotLogPW.println(spot.getAgents().size());

                // 他のスポットの情報もここで表示できます
            }


        }



        // *************************************************************************************************************
        // シミュレーションの終了処理
        // *************************************************************************************************************

        ruleExecutor.shutdown();
        spotLogPW.close();

        try {
            SpotDataProcessor processor = new SpotDataProcessor(pathOfLogDir  + "database.db", pathOfLogDir  + "spot_log.csv");
            processor.process();
        } catch (SQLException | IOException e) {
            e.printStackTrace();

        }

        DatabaseReader reader = new DatabaseReader(pathOfLogDir+"database.db");
        reader.readAndPrintBuildingRequests();

    }
}

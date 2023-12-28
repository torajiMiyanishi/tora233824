import jp.soars.core.TObject;
import jp.soars.core.TRole;
import jp.soars.core.TSpot;


/**
 * スポットの役割を定義するクラス
 */
public class TRoleOfSpot extends TRole {

    // スポットのX座標
    private int x;

    // スポットのY座標
    private int y;

    /**
     * コンストラクタ
     * @param x スポットのX座標
     * @param y スポットのY座標
     */
    public TRoleOfSpot(TSpot spot, int x, int y) {
        super(ERoleName.Spot, spot);
        this.x = x;
        this.y = y;

    }

    // X座標を取得する
    public int getX() {
        return x;
    }

    // Y座標を取得する
    public int getY() {
        return y;
    }

    // その他のメソッドや機能をここに追加
}
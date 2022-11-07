package diarsid.support.javafx.mouse;

import diarsid.support.objects.CommonEnum;

import static java.lang.Integer.MAX_VALUE;

public enum ClickType implements CommonEnum<ClickType> {

    USUAL_CLICK (MAX_VALUE),
    SEQUENTIAL_CLICK (1000),
    DOUBLE_CLICK (200);

    private int msAfterLastClick;

    ClickType(int msAfterLastClick) {
        this.msAfterLastClick = msAfterLastClick;
    }

    public int msAfterLastClick() {
        return this.msAfterLastClick;
    }
}

package model;

import java.util.logging.Logger;

public class Button {

    private int row;
    private int col;

    public Button(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public double computeDistance(Button button){
        if(row == button.getRow()) {
            int diff = Math.abs(col - button.getCol());
            return diff * 2;
        }
        if(col == button.getCol()) {
            int diff = Math.abs(row - button.getRow());
            return diff * 2;
        }
        //Diagonali
        for(int i=row, j=col; i>=0 && j<5; i--, j++){
            if(button.getRow()==i && button.getCol()==j)
                return 2 * (row-i);
        }
        for(int i=row, j=col; i<23 && j>=0; i++, j--){
            if(button.getRow()==i && button.getCol()==j)
                return 2 * (col-j);
        }
        for(int i=row, j=col; i>=0 && j>=0; i--, j--){
            if(button.getRow()==i && button.getCol()==j)
                return 3 * (row-i);
        }
        for(int i=row, j=col; i<23 && j<5; i++, j++){
            if(button.getRow()==i && button.getCol()==j)
                return 3 * (j-col);
        }
        //Altre configurazioni
        double d = computeSpecialConfigurationsDistance(row, col, button.getRow(), button.getCol());
        return d>0?d:computeSpecialConfigurationsDistance(button.getRow(), button.getCol(), row, col);
    }

    private double computeSpecialConfigurationsDistance(int ar, int ac, int br, int bc){

        int cd = ac-bc;
        int rd = br-ar;

        //Computa solo le configurazioni "a scendere"
        if(rd<0)    return -1;

        if(cd==1){
            return 3 + (rd-2) * 1.75;
        }
        if(cd==-1){
            return 3 + (rd-1) * 1.75;
        }
        if(cd==2){
            if(rd==1)    return 3.5;
            if(rd>=2)    return 3.5 + (rd-2) * 1.5;
        }
        if(cd==-2){
            return 5 + (rd-1) * 1.5;
        }
        if(cd==3){
            if(rd==1)    return 5.5;
            if(rd>=2)    return 5.5 + (rd-2);
        }
        if(cd==-3){
            return 6.5 + (rd-1) * 1.5;
        }
        if(cd==4){
            if(rd<4)    return 6.5;
            if(rd>=4)   return 6.5 + (rd-3);
        }
        if(cd==-4){
            return 8.5 + (rd-1) * 1.5;
        }

        Logger logger = Logger.getLogger("Model.Button distance");
        logger.warning("Model.Button configuration not found.");
        return -1;
    }
}
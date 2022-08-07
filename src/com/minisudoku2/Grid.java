package com.minisudoku2;

public class Grid {
    public static final int Grid_Size = 9;
    public static final int Box_Size = 3;
    private int[][] mat = new int[Grid_Size][Grid_Size];
    private int[][] pos = new int[Grid_Size][Grid_Size];
    private boolean solved;

    public Grid(String input) throws IllegalArgumentException{
        for(int i = 0; i < Grid_Size; i++){
            for(int j = 0; j < Grid_Size; j++){
                pos[i][j] = 0b111111111;
            }
        }
        if (input==null || input.length()!= Grid_Size * Grid_Size||!input.matches("[0-9]+"))
            throw new IllegalArgumentException(
                    String.format("Invalid input! Enter single digit (0 to %d) only. Enter 0 for empty cell.",
                    Grid_Size));
        for(int idx = 0; idx < input.length(); idx++) {
            int row = idx / 9; int col = idx % 9;
            int e = Integer.parseInt(input.substring(idx, idx + 1));
            if (e != 0) {
                if (!getBit(pos[row][col], e - 1))
                    throw new IllegalArgumentException("Invalid input! Entered digits collide.");
                fillANDelim(row, col, e);
            }
        }
    }

    private boolean fillANDelim(int row, int col, int e){
        mat[row][col] = e;
        pos[row][col] = 0;
        for (int i = 0; i < Grid_Size; i++){
            pos[row][i] = setBit(pos[row][i], e-1, false);
            pos[i][col] = setBit(pos[i][col], e-1, false);
        }
        int boxStartRow = row - row % Box_Size; int boxStartCol = col - col % Box_Size;
        for (int i = boxStartRow; i < boxStartRow + Box_Size; i++){
            for (int j = boxStartCol; j < boxStartCol + Box_Size; j++)
                pos[i][j] = setBit(pos[i][j], e-1, false);
        }
        pos[row][col] = setBit(pos[row][col], e-1, true);
        return true;
    }

    private boolean findOneCell(int row, int col){
        int[] coord = getFirstEmpty(row, col);
        if (coord == null) return true;
        int curRow = coord[0]; int curCol = coord[1];
        for(int e = 1; e <= Grid_Size; e++){
            if (!getBit(pos[curRow][curCol],e-1) || !check(curRow,curCol,e))
                continue;
            //System.out.println(this);
            mat[curRow][curCol] = e;
            int[] nextCoord = getNextCell(curRow,curCol);
            if (nextCoord == null) return true;
            int nextRow = nextCoord[0]; int nextCol = nextCoord[1];
            if (findOneCell(nextRow, nextCol)) return true;
            mat[curRow][curCol] = 0;
        }
        return false;
    }

    private boolean harvestOnes(){
        boolean bettered = false;
        for (int i = 0; i < Grid_Size; i++){
            for (int j = 0; j < Grid_Size; j++){
                if (mat[i][j]!=0) continue;
                bettered |= switch(pos[i][j]){
                    case(0b000000001) -> fillANDelim(i,j,1);
                    case(0b000000010) -> fillANDelim(i,j,2);
                    case(0b000000100) -> fillANDelim(i,j,3);
                    case(0b000001000) -> fillANDelim(i,j,4);
                    case(0b000010000) -> fillANDelim(i,j,5);
                    case(0b000100000) -> fillANDelim(i,j,6);
                    case(0b001000000) -> fillANDelim(i,j,7);
                    case(0b010000000) -> fillANDelim(i,j,8);
                    case(0b100000000) -> fillANDelim(i,j,9);
                    default -> false;
                };
            }
        }
        return bettered;
    }

    private boolean exclusionOnes(){
        boolean bettered = false;
        for (int i = 0; i < Grid_Size; i++){
            for (int e = 0; e < Grid_Size; e++){
                int markCol = -1;
                for (int j = 0; j < Grid_Size; j++){
                    if(mat[i][j]==0 && getBit(pos[i][j],e-1)) {
                        if(markCol < 0) markCol = j;
                        else {markCol = -1; break;}
                    }
                }
                if (markCol > 0) bettered |= fillANDelim(i,markCol,e);
            }
        }
        for (int j = 0; j < Grid_Size; j++){
            for (int e = 0; e < Grid_Size; e++){
                int markRow = -1;
                for (int i = 0; i < Grid_Size; i++){
                    if(mat[i][j]==0 && getBit(pos[i][j],e-1)) {
                        if(markRow < 0) markRow = i;
                        else {markRow = -1; break;}
                    }
                }
                if (markRow > 0) bettered |= fillANDelim(markRow,j,e);
            }
        }
        for (int outIdx = 0; outIdx < Grid_Size; outIdx++){
            int startRow = outIdx - outIdx % Box_Size, startCol = outIdx % Box_Size * Box_Size;
            for (int e = 0; e < Grid_Size; e++){
                int markRow = -1, markCol = -1;
                for (int inIdx = 0; inIdx < Grid_Size; inIdx++){
                    int row = startRow + inIdx / Box_Size, col = startCol + inIdx % Box_Size;
                    if(mat[row][col]==0 && getBit(pos[row][col],e-1)) {
                        if(markRow < 0 && markCol < 0 ) {markRow = row; markCol = col;}
                        else {markRow = -1; markCol = -1; break;}
                    }
                }
                if (markRow > 0 && markCol > 0) bettered |= fillANDelim(markRow,markCol,e);
            }
        }
        return bettered;
    }

    public void calculate(){
        while(harvestOnes());
        while(exclusionOnes()){
            while(harvestOnes());
        }
        findOneCell(0,0);
        for(int[] row : mat){
            for (int ele: row)
                if (ele==0) return;
        }
        solved = true;
    }

    private int[] getFirstEmpty(int row, int col){//return null if not found
        for(int i = row; i < Grid_Size; i++){
            for (int j = 0; j < Grid_Size; j++){
                if (mat[i][j]==0) return new int[]{i,j};
            }
        }
        return null;
    }

    private int[] getNextCell(int row, int col){
        if (row >= Grid_Size - 1  && col >= Grid_Size - 1) return null;
        else if (col == Grid_Size - 1) return new int[]{row + 1, 0};
        else return new int[]{row, col + 1};
    }

    public boolean check(int row, int col, int attempt){
        Boolean legal = true;
        for (int i = 0; i < Grid_Size; i++){
            if(i!=row && (mat[i][col] == attempt)) return false;
            if(i!=col && (mat[row][i] == attempt)) return false;
        }
        int boxStartRow = row - row % Box_Size; int boxStartCol = col - col % Box_Size;
        for (int i = boxStartRow; i < boxStartRow + Box_Size; i++){
            for (int j = boxStartCol; j < boxStartCol + Box_Size; j++)
                if((i!=row || j!=col) && mat[i][j] == attempt) return false;
        }
        return true;
    }

    private boolean getBit(int B, int bit){
        return (1 & (B >> bit)) == 1;
    }

    private int setBit(int B, int bit, boolean desired){
        if(desired){
            B |= 1 << bit;
        }else{
            B &= ~(1 << bit);
        }
        return B;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        for (int[] row : mat) {
            for (int ele : row) {
                sb.append(ele); sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getAnsString(){
        StringBuilder sb = new StringBuilder("");
        for (int[] row : mat) {
            for (int ele : row) {
                sb.append(ele);
            }
        }
        return sb.toString();
    }

    public boolean isSolved(){return solved;}
}
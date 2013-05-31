/*
 * Copyright (c) 2013, Keeley Hoek
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the
 * following disclaimer.
 *
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.escortkeel.kchess;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.HashMap;
import javax.imageio.ImageIO;
import static com.github.escortkeel.kchess.Piece.KING;
import static com.github.escortkeel.kchess.Piece.ROOK;

/**
 *
 * @author Keeley Hoek (escortkeel@gmail.com)
 */
public class GameManager {

    public static final String IMAGESUFFIX = ".gif";
    private final HashMap<String, Image> imageMap = new HashMap();
    private final String teams[] = {"b", "w"};
    private final String pieces[] = {"b", "k", "n", "p", "q", "r"};
    private int[] dR = {1, -1, 0, 0, 1, 1, -1, -1};
    private int[] dC = {0, 0, 1, -1, 1, -1, 1, -1};
    private int castleVal = -1;

    public GameManager() throws IOException {
        for (String team : teams) {
            for (String piece : pieces) {
                imageMap.put(team + piece, ImageIO.read(GameManager.class.getResourceAsStream("/" + team + piece + IMAGESUFFIX)));
            }
        }
    }

    public boolean isInBounds(int r, int c) {
        return ((r >= 0) && (r < 8)) && ((c >= 0) && (c < 8));
    }

    public int didCastle() {
        int temp = castleVal;
        castleVal = -1;
        return temp;
    }

    public void drawBackground(Graphics g, Tile[][] tiles, int selectedR, int selectedC, int r, int c) {
        if (selectedR == r && selectedC == c) {
            g.setColor(Color.YELLOW);
        } else if (canMove(tiles, selectedR, selectedC, r, c, false)) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(((r + c) % 2) == 1 ? Color.GRAY : Color.WHITE);
        }

        g.fillRect(c * 75, r * 75, 75, 75);
    }

    public void drawForeground(Graphics g, Tile t, int r, int c) {
        if (t.getTeam() == Team.NONE) {
            return;
        }

        g.drawImage(imageMap.get(t.getTeam().toString() + t.getPiece().toString()), c * 75, r * 75, null);
    }

    public boolean isOver(Tile[][] board) {
        boolean whiteKingFound = false, blackKingFound = false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j].getTeam() == Team.WHITE) {
                    if (board[i][j].getPiece() == Piece.KING) {
                        whiteKingFound = true;
                    }
                }

                if (board[i][j].getTeam() == Team.BLACK) {
                    if (board[i][j].getPiece() == Piece.KING) {
                        blackKingFound = true;
                    }
                }
            }
        }

        return !whiteKingFound || !blackKingFound;
    }

    public boolean canMove(Tile[][] tiles, int selectedR, int selectedC, int targetR, int targetC, boolean excecute) {
        if (selectedR == -1 || selectedC == -1) {
            return false;
        }

        try {
            if (tiles[selectedR][selectedC].getTeam() != Team.NONE) {
                int direction = tiles[selectedR][selectedC].getTeam() == Team.WHITE ? -1 : 1;

                switch (tiles[selectedR][selectedC].getPiece()) {
                    case PAWN:
                        return canPawn(tiles, selectedR, selectedC, targetR, targetC, direction);
                    case KING:
                        return canKing(tiles, selectedR, selectedC, targetR, targetC, direction, excecute);
                    case QUEEN:
                        return canBishop(tiles, selectedR, selectedC, targetR, targetC, direction)
                                || canRook(tiles, selectedR, selectedC, targetR, targetC, direction);
                    case BISHOP:
                        return canBishop(tiles, selectedR, selectedC, targetR, targetC, direction);
                    case ROOK:
                        return canRook(tiles, selectedR, selectedC, targetR, targetC, direction);
                    case KNIGHT:
                        return canKnight(tiles, selectedR, selectedC, targetR, targetC, direction);
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    private boolean canPawn(Tile[][] tiles, int selectedR, int selectedC, int targetR, int targetC, int direction) {
        if (tiles[selectedR + direction][selectedC].getTeam() == Team.NONE) {
            if (selectedR + direction == targetR && selectedC == targetC) {
                return true;
            } else if (!tiles[selectedR][selectedC].hasMoved() && selectedR + (2 * direction) == targetR && selectedC == targetC) {
                return true;
            }
        }

        if (selectedC + 1 == targetC && selectedR + direction == targetR) {
            if (tiles[selectedR + direction][selectedC + 1].getTeam().ordinal() > 0
                    && tiles[selectedR + direction][selectedC + 1].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        if (selectedC - 1 == targetC && selectedR + direction == targetR) {
            if (tiles[selectedR + direction][selectedC - 1].getTeam().ordinal() > 0
                    && tiles[selectedR + direction][selectedC - 1].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        return false;
    }

    private boolean canKing(Tile[][] tiles, int selectedR, int selectedC, int targetR, int targetC, int direction, boolean excecute) {
        for (int i = 0; i < 8; i++) {
            if ((selectedR + dR[i]) == targetR && (selectedC + dC[i]) == targetC) {
                if (tiles[targetR][targetC].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                    return true;
                }

                break;
            }
        }

        if (!tiles[selectedR][selectedC].hasMoved()) {
            if (!tiles[selectedR][0].hasMoved()) {
                if (tiles[selectedR][1].getTeam() == Team.NONE
                        && tiles[selectedR][2].getTeam() == Team.NONE
                        && tiles[selectedR][3].getTeam() == Team.NONE) {
                    if (targetR == selectedR && targetC == 2) {
                        if (excecute) {
                            castleVal = 2;
                        }
                        return true;
                    }
                }
            }

            if (!tiles[selectedR][7].hasMoved()) {
                if (tiles[selectedR][6].getTeam() == Team.NONE
                        && tiles[selectedR][5].getTeam() == Team.NONE) {
                    if (targetR == selectedR && targetC == 6) {
                        if (excecute) {
                            castleVal = 6;
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean canKnight(Tile[][] tiles, int selectedR, int selectedC, int targetR, int targetC, int direction) {
        if ((Math.abs(selectedR - targetR) == 1 && Math.abs(selectedC - targetC) == 2)
                || (Math.abs(selectedR - targetR) == 2 && Math.abs(selectedC - targetC) == 1)) {
            if (tiles[targetR][targetC].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        return false;
    }

    private boolean canBishop(Tile[][] tiles, int selectedR, int selectedC, int targetR, int targetC, int direction) {
        int delta;
        for (delta = 1; delta < 8; delta++) {
            if (!isInBounds(selectedR + delta, selectedC + delta)) {
                break;
            }

            if (tiles[selectedR + delta][selectedC + delta].getTeam() != Team.NONE) {
                break;
            }

            if ((selectedR + delta) == targetR && (selectedC + delta) == targetC) {
                return true;
            }
        }

        if (isInBounds(selectedR + delta, selectedC + delta)) {
            if (selectedR + delta == targetR && selectedC + delta == targetC
                    && tiles[selectedR + delta][selectedC + delta].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }


        for (delta = 1; delta < 8; delta++) {
            if (!isInBounds(selectedR + delta, selectedC - delta)) {
                break;
            }

            if (tiles[selectedR + delta][selectedC - delta].getTeam() != Team.NONE) {
                break;
            }

            if ((selectedR + delta) == targetR && (selectedC - delta) == targetC) {
                return true;
            }
        }

        if (isInBounds(selectedR + delta, selectedC - delta)) {
            if (selectedR + delta == targetR && selectedC - delta == targetC
                    && tiles[selectedR + delta][selectedC - delta].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        for (delta = 1; delta < 8; delta++) {
            if (!isInBounds(selectedR - delta, selectedC + delta)) {
                break;
            }

            if (tiles[selectedR - delta][selectedC + delta].getTeam() != Team.NONE) {
                break;
            }

            if ((selectedR - delta) == targetR && (selectedC + delta) == targetC) {
                return true;
            }
        }

        if (isInBounds(selectedR - delta, selectedC + delta)) {
            if (selectedR - delta == targetR && selectedC + delta == targetC
                    && tiles[selectedR - delta][selectedC + delta].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        for (delta = 1; delta < 8; delta++) {
            if (!isInBounds(selectedR - delta, selectedC - delta)) {
                break;
            }

            if (tiles[selectedR - delta][selectedC - delta].getTeam() != Team.NONE) {
                break;
            }

            if ((selectedR - delta) == targetR && (selectedC - delta) == targetC) {
                return true;
            }
        }

        if (isInBounds(selectedR - delta, selectedC - delta)) {
            if (selectedR - delta == targetR && selectedC - delta == targetC
                    && tiles[selectedR - delta][selectedC - delta].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        return false;
    }

    private boolean canRook(Tile[][] tiles, int selectedR, int selectedC, int targetR, int targetC, int direction) {
        int checkR;
        for (checkR = selectedR + 1; checkR < 8; checkR++) {
            if (tiles[checkR][selectedC].getTeam() != Team.NONE) {
                break;
            }

            if (checkR == targetR && selectedC == targetC) {
                return true;
            }
        }

        if (checkR < 8) {
            if (checkR == targetR && selectedC == targetC
                    && tiles[checkR][selectedC].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        for (checkR = selectedR - 1; checkR >= 0; checkR--) {
            if (tiles[checkR][selectedC].getTeam() != Team.NONE) {
                break;
            }

            if (checkR == targetR && selectedC == targetC) {
                return true;
            }
        }

        if (checkR >= 0) {
            if (checkR == targetR && selectedC == targetC
                    && tiles[checkR][selectedC].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        int checkC;
        for (checkC = selectedC + 1; checkC < 8; checkC++) {
            if (tiles[selectedR][checkC].getTeam() != Team.NONE) {
                break;
            }

            if (checkC == targetC && selectedR == targetR) {
                return true;
            }
        }

        if (checkC < 8) {
            if (checkC == targetC && selectedR == targetR
                    && tiles[selectedR][checkC].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        for (checkC = selectedC - 1; checkC >= 0; checkC--) {
            if (tiles[selectedR][checkC].getTeam() != Team.NONE) {
                break;
            }

            if (checkC == targetC && selectedR == targetR) {
                return true;
            }
        }

        if (checkC >= 0) {
            if (checkC == targetC && selectedR == targetR
                    && tiles[selectedR][checkC].getTeam() != tiles[selectedR][selectedC].getTeam()) {
                return true;
            }
        }

        return false;
    }
}

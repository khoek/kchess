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

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Keeley Hoek (escortkeel@gmail.com)
 */
public class BoardPanel extends JPanel {

    private Team currentTeam;
    private Tile[][] board = new Tile[8][8];
    private final GameFrame game;
    private int selectedR, selectedC;
    private boolean gameEnded;

    public BoardPanel(GameFrame game) {
        this.game = game;
        this.selectedR = -1;
        this.selectedC = -1;

        reset();

        final BoardPanel boardPanel = this;

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameEnded) {
                    return;
                }

                int newC = e.getX() / 75, newR = e.getY() / 75;

                if (selectedR == -1 && board[newR][newC].getTeam() == currentTeam) {
                    selectedC = newC;
                    selectedR = newR;
                } else if (selectedR != -1 && boardPanel.game.getGameManager().canMove(board, selectedR, selectedC, newR, newC, true)) {
                    int castledTo = boardPanel.game.getGameManager().didCastle();
                    if (castledTo != -1) {
                        if (castledTo == 2) {
                            board[currentTeam == Team.WHITE ? 7 : 0][0].setTeam(Team.NONE);

                            board[currentTeam == Team.WHITE ? 7 : 0][3].setTeam(currentTeam);
                            board[currentTeam == Team.WHITE ? 7 : 0][3].setPiece(Piece.ROOK);
                            board[currentTeam == Team.WHITE ? 7 : 0][3].setMoved(true);
                        } else if (castledTo == 6) {
                            board[currentTeam == Team.WHITE ? 7 : 0][7].setTeam(Team.NONE);

                            board[currentTeam == Team.WHITE ? 7 : 0][5].setTeam(currentTeam);
                            board[currentTeam == Team.WHITE ? 7 : 0][5].setPiece(Piece.ROOK);
                            board[currentTeam == Team.WHITE ? 7 : 0][5].setMoved(true);
                        }
                    }

                    board[newR][newC].setTeam(board[selectedR][selectedC].getTeam());
                    board[newR][newC].setPiece(board[selectedR][selectedC].getPiece());
                    board[newR][newC].setMoved(true);

                    board[selectedR][selectedC].setTeam(Team.NONE);

                    selectedC = -1;
                    selectedR = -1;

                    if (currentTeam == Team.WHITE) {
                        currentTeam = Team.BLACK;
                    } else {
                        currentTeam = Team.WHITE;
                    }

                    boardPanel.game.setTurn(currentTeam);
                } else if (selectedC == newC && selectedR == newR) {
                    selectedC = -1;
                    selectedR = -1;
                }

                boardPanel.repaint();

                if (boardPanel.game.getGameManager().isOver(board)) {
                    JOptionPane.showMessageDialog(boardPanel, (currentTeam == Team.BLACK ? "White" : "Black") + " wins! Congratulations!", "Game over!", JOptionPane.WARNING_MESSAGE);
                    gameEnded = true;
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                game.getGameManager().drawBackground(g, board, selectedR, selectedC, r, c);
            }
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                game.getGameManager().drawForeground(g, board[r][c], r, c);
            }
        }
    }

    public final void reset() {
        this.gameEnded = false;
        this.currentTeam = Team.WHITE;

        game.setTurn(Team.WHITE);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = new Tile();
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j].setTeam(Team.BLACK);
            }
        }

        for (int i = 0; i < 8; i++) {
            board[1][i].setPiece(Piece.PAWN);
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                board[6 + i][j].setTeam(Team.WHITE);
            }
        }

        for (int i = 0; i < 8; i++) {
            board[6][i].setPiece(Piece.PAWN);
        }

        board[0][0].setPiece(Piece.ROOK);
        board[0][1].setPiece(Piece.KNIGHT);
        board[0][2].setPiece(Piece.BISHOP);
        board[0][3].setPiece(Piece.QUEEN);
        board[0][4].setPiece(Piece.KING);
        board[0][5].setPiece(Piece.BISHOP);
        board[0][6].setPiece(Piece.KNIGHT);
        board[0][7].setPiece(Piece.ROOK);

        board[7][0].setPiece(Piece.ROOK);
        board[7][1].setPiece(Piece.KNIGHT);
        board[7][2].setPiece(Piece.BISHOP);
        board[7][3].setPiece(Piece.QUEEN);
        board[7][4].setPiece(Piece.KING);
        board[7][5].setPiece(Piece.BISHOP);
        board[7][6].setPiece(Piece.KNIGHT);
        board[7][7].setPiece(Piece.ROOK);

        repaint();
    }
}

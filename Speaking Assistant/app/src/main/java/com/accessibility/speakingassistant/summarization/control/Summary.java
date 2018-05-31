/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.accessibility.speakingassistant.summarization.control;

import com.accessibility.speakingassistant.summarization.model.Document;
import com.accessibility.speakingassistant.summarization.model.Edge;
import com.accessibility.speakingassistant.summarization.model.Sentence;
import com.accessibility.speakingassistant.summarization.model.Word;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import vn.hus.nlp.tokenizer.VietTokenizer;
public class Summary {

    private VietTokenizer tokenizer;
    private  String regex;
    private  Context mContext;

    private String dirStopword = "file:///android_asset/vnstopwords.txt";

    public Summary(Context context) {
        tokenizer = new VietTokenizer(context);
        mContext = context;

        regex = "\\@|\\#|\\$|\\%|\\^|\\&|\\*|\\(|\\)|\\-|\\+|\\-|\\=|\\[|\\]|\\{|\\}|\\;|\\:|\\\"|\\'|\\<|\\>|\\/|\\,|\\&|\\+d.+d|\\d|\\/|\\“|\\”|\\.";
    }

    private  String ReadFileText() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream("F:\\Data\\datasample\\donvanban\\Processedtext\\boKHCN\\khcn1.txt")));
            String text = "";
            String aLine = null;
            while ((aLine = in.readLine()) != null) {
                text += aLine + "\n";
            }
            in.close();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // read stop word
    public  ArrayList<String> readStopWord() {
        try {
//            mContext.getAssets();
            String actualFilename = dirStopword.split("file:///android_asset/")[1];
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open(actualFilename)));
            ArrayList<String> stopWord = new ArrayList<>();
//            Log.e("Global_button","Open file");
            String text = "";
            String aLine = null;
            while ((aLine = in.readLine()) != null) {
                stopWord.add(aLine);
            }
            in.close();
            return stopWord;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private  void WriteFile(String text) {
        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("F:\\Data\\doc_new.txt")));
            System.out.print(text);
            out.write(text);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int hasWord(ArrayList<Word> arrW, String w) {
        for (int i = 0; i < arrW.size(); i++) {
            if (w.equals(arrW.get(i).getW()))
                return i;
        }
        return -1;
    }

    private static boolean isStopWord(ArrayList<String> arrStr, String w) {
        for (int i = 0; i < arrStr.size(); i++) {
            if (w.equals(arrStr.get(i)))
                return true;
        }
        return false;
    }

    /**
     * @param token
     */

    // count word
    private Document process(ArrayList<String> sentences, ArrayList<String> token,
                             ArrayList<String> arrStopWord) {
        Document document = new Document();
        ArrayList<Sentence> arrS = new ArrayList<>();
        ArrayList<Word> arrW = new ArrayList<>();
        ArrayList<Word> arrWordAll = new ArrayList<>();

        int numberSentence = 0;
        int numberWord = 0;

        for (int i = 0; i < sentences.size(); i++) {
            //System.out.println(sentences.get(i));
//            String[] tmp = tokenizer.tokenize(sentences.get(i));
            //System.out.print(tmp[0]);

            String s = "";
            ArrayList<String> ans = (ArrayList<String>) tokenizer.tokenize(sentences.get(i));
            for (String str : ans){
                s+= str + " ";
            }
            String r = s.replace(regex,"");
//            String r = tmp[0].replaceAll(regex, "");
            //System.out.println(r);
            String[] tt = r.split(" ");
            int k;
            if (i==0)	k=1;
            else k=0;
            numberWord = 0;
            for (int j = k; j < tt.length; j++) {
                String str = tt[j].toLowerCase();
                //System.out.println(str + str.length());
                if (!str.equals("\n") && !str.equals("")) {
                    //System.out.println(str);
                    if (isStopWord(arrStopWord, str) == false) {
                        numberWord++;
                        int check = hasWord(arrW, str);
                        if (check == -1) {

                            Word w = new Word();
                            w.setW(str);
                            w.setF(1);
                            arrW.add(w);

                            // check word in doc
                            int check_ = hasWord(arrWordAll, str);
                            if (check_ == -1) {
                                Word wo = new Word();
                                wo.setW(str);
                                wo.setF(1);
                                arrWordAll.add(wo);
                            } else {
                                Word wo = arrWordAll.get(check_);
                                wo.setF(wo.getF() + 1);
                            }

                        } else {
                            Word w = arrW.get(check);
                            w.setF(w.getF() + 1);
                            int check_ = hasWord(arrWordAll, str);
                            Word wo = arrWordAll.get(check_);
                            wo.setF(wo.getF() + 1);
                        }
                    }
                }

            }
            Sentence sentence = new Sentence();
            sentence.setId(arrS.size() + 1);
            sentence.setNumberWord(numberWord);
            sentence.setArrW(arrW);
            arrS.add(sentence);
            arrW = new ArrayList<>();
        }

//		 for (int i = 0; i < token.size(); i++) {
//		 // System.out.println(str);
//		 String str = token.get(i);
//		 // str = str.replaceAll("_", "");
//		 if (str.equals(".") || str.equals("?") || str.equals("...") ||
//		 str.equals("!") || str.equals("…")) {
//		 // System.out.println("Cham: " +str);
//		 if (numberWord != 0) {
//		 if (arrW.size() > 1) {
//		 numberSentence++;
//		 Sentence sentence = new Sentence();
//		 sentence.setId(arrS.size() + 1);
//		 sentence.setNumberWord(numberWord);
//		 sentence.setArrW(arrW);
//		 arrS.add(sentence);
//		 numberWord = 0;
//		 // for (Word w : arrW) System.out.print(w.getW() + " ");
//
//		 arrW = new ArrayList<>();
//		 }
//		 }
//		 } else if (isStopWord(arrStopWord, str) == false) {
//
//		 // check word in sentence
//		 numberWord++;
//		 int check = hasWord(arrW, str);
//
//		 if (check == -1) {
//
//		 Word w = new Word();
//		 w.setW(str);
//		 w.setF(1);
//		 arrW.add(w);
//
//		 // check word in doc
//		 int check_ = hasWord(arrWordAll, str);
//		 if (check_ == -1) {
//		 Word wo = new Word();
//		 wo.setW(str);
//		 wo.setF(1);
//		 arrWordAll.add(wo);
//		 } else {
//		 Word wo = arrWordAll.get(check_);
//		 wo.setF(wo.getF() + 1);
//		 }
//
//		 } else {
//		 Word w = arrW.get(check);
//		 w.setF(w.getF() + 1);
//		 }
//
//		 }
//
//		 }

        // print sentence
//		for (Sentence s : arrS) {
//			System.out.println("\nSentence " + s.getId() + "-" + s.getNumberWord());
//			for (Word w : s.getArrW()) {
//				System.out.print(w.getW() + " " );
//			}
//			System.out.println();
//		}

        // AllWord
//		 System.out.println("All " + sentences.size() );
//		 for (Word w : arrWordAll) System.out.println(w.getW() + "\t" + w.getF());

        // set document
        document.setNumberSentence(sentences.size());
        document.setArrS(arrS);
        document.setArrWordAll(arrWordAll);
        return document;

    }

    // cal tfidf for word in sentence
    private  void calTfidf(Document document) {
        int numberSentence = document.getNumberSentence();
        ArrayList<Word> arrWordAll = document.getArrWordAll();
        for (Sentence s : document.getArrS()) {
            int numberWord = s.getNumberWord();
            for (Word w : s.getArrW()) {
                double tf = w.getF() * 1.00 / numberWord;
                int check = hasWord(arrWordAll, w.getW());

                // System.out.println(w.getW() + " "+ numberSentence*1.00 /
                // arrWordAll.get(check).getF() );

                double idf = Math.log10(numberSentence * 1.00 / arrWordAll.get(check).getF());

                double tfidf = tf * idf;
                w.setTfidf(tfidf);
                // System.out.println(w.getW() + ": tf " + tf + " idf " + idf + " tfidf "+ tfidf);
            }
        }

        // print
        // for (Sentence s : document.getArrS()) {
        // System.out.println("\t Sentence " + s.getId());
        // for (Word w : s.getArrW()) {
        // System.out.println(w.getW() + "\t" + w.getTfidf());
        // }
        // }
    }

    // cosine distance
    public  double similarity(Sentence s1, Sentence s2) {
        double t = 0;
        double dis1 = 0;
        double dis2 = 0;
        for (Word w : s1.getArrW()) {
            int check = hasWord(s2.getArrW(), w.getW());
            if (check != -1) {
                //System.out.println(w.getW());
                t += w.getTfidf() * s2.getArrW().get(check).getTfidf();
            }
            dis1 += w.getTfidf() * w.getTfidf();
        }
        for (Word w : s2.getArrW()) {
            dis2 += w.getTfidf() * w.getTfidf();
        }
        //System.out.println(t + " " + " " + dis1 + " " + dis2);
        double simi = t / (Math.sqrt(dis1 * dis2));
        return simi;
    }

    // construct graph
    public  double[][] contructGraph(Document document) {
        double[][] graph = new double[100][100];
        ArrayList<Sentence> arrS = document.getArrS();
        for (int i = 0; i < arrS.size(); i++) {
            Sentence s1 = arrS.get(i);
            graph[i + 1][i + 1] = 0;
            for (int j = i + 1; j < arrS.size(); j++) {
                Sentence s2 = arrS.get(j);
                double simi = similarity(s1, s2);
                // System.out.println(simi);
                graph[i + 1][j + 1] = simi;
                graph[j + 1][i + 1] = simi;
            }
        }

        // print
//		System.out.println("Graph ");
//		System.out.print("\t");
//		for (int i = 1; i <= arrS.size(); i++)
//			System.out.print(i + "\t\t");
//		System.out.println();
//		for (int i = 1; i <= arrS.size(); i++) {
//			System.out.print(i + "\t");
//			for (int j = 1; j <= arrS.size(); j++) {
//				System.out.printf("%.5f \t", graph[i][j]);
//				// System.out.print(graph[i][j] + "\t");
//			}
//			System.out.println();
//		}
        return graph;
    }

    // count degree
    public int[] degree(double graph[][], Document document) {
        int numberSentence = document.getNumberSentence();
        int degree[] = new int[100];
        for (int i = 1; i <= numberSentence; i++) {
            int dem = 0;
            for (int j = 1; j <= numberSentence; j++) {
                if (graph[i][j] != 0 && i != j)
                    dem++;
            }
            degree[i] = dem;
        }
        // for (int i=1; i<=numberSentence; i++) System.out.println("Degree " + i +": "
        // +degree[i] );
        return degree;
    }

    // pagerank algorithm
    public double[] pagerank(double graph[][], int degree[], Document document) {

        float D = (float) 0.85;
        int numberSentence = document.getNumberSentence();

        // System.out.println("Graph: ");
        for (int i = 1; i <= numberSentence; i++) {
            for (int j = 1; j <= numberSentence; j++) {
                // System.out.print(graph[i][j] + "\t");
            }
            // System.out.println();
        }

        // System.out.println("Degree: ");
        // for (int i=1; i<=numberSentence; i++) System.out.print(degree[i] + "\t");
        // System.out.println();

        double prIntial = 0.00;// 1.00/numberSentence;
        double pr[] = new double[numberSentence + 7];
        double prNew[] = new double[numberSentence + 7];
        for (int i = 1; i <= numberSentence; i++) {
            pr[i] = prIntial;
            prNew[i] = prIntial;
        }
        int iteration = 1;
        while (true) {
            for (int i = 1; i <= numberSentence; i++) {
                prNew[i] = 0;
                for (int j = 1; j <= numberSentence; j++) {
                    if (graph[i][j] != 0)
                        prNew[i] += prNew[j] * graph[i][j] / degree[j];
                }
                prNew[i] = (1 - D) + D * prNew[i];

            }
            int dem = 0;
            System.out.println("Iteration: " + iteration);
            iteration++;
            for (int i = 1; i <= numberSentence; i++) {
                double d = Math.abs(pr[i] - prNew[i]);
                if (d <= 0.000001)
                    dem++;
                pr[i] = prNew[i];
                //System.out.print(pr[i] + "\t");
            }
            //System.out.println();
            if (iteration > 100)
                break;
            if (dem > (numberSentence / 2)) {
                break;
            }
        }
        return prNew;
    }

    public boolean checkSimi(ArrayList<Integer> sentenceSelected, double graph[][], int newSentence, double d) {
        for (int i : sentenceSelected) {
            if (graph[newSentence][i] > d)
                return false;
        }
        return true;
    }

    public ArrayList<Integer> selectSentence(double[] pagerank, double graph[][], Document doc,
                                                    int sentenceCount, double similarity) {
        int numberSentence = doc.getNumberSentence();
        // double num = n*numberSentence;
        // int numberSelect = (int)(n*numberSentence);
        int numberSelect = sentenceCount;
        // System.out.println(doc.getNumberSentence() + " " + numberSelect + " "+ num);
        ArrayList<Edge> arr = new ArrayList<>();

        for (int i = 1; i <= numberSentence; i++) {
            Edge e = new Edge(i, pagerank[i]);
            arr.add(e);
        }

        Collections.sort(arr, new Comparator<Edge>() {
            @Override
            public int compare(Edge o1, Edge o2) {
                // TODO Auto-generated method stub
                if (o1.getPagerank() < o2.getPagerank())
                    return 1;
                else if (o1.getPagerank() > o2.getPagerank())
                    return -1;
                else
                    return 0;
            }
        });

        // print
        // for (Edge e : arr) {
        // System.out.println(e.getSentence() + "\t" + e.getPagerank());
        // }

        ArrayList<Integer> sentenceSelected = new ArrayList<>();

        int j = 0;
        while (true) {
            if (j == 0) {
                sentenceSelected.add(arr.get(j).getSentence());
                j++;
            } else {
                Edge e = arr.get(j);
                j++;
                if (checkSimi(sentenceSelected, graph, e.getSentence(), similarity)) {
                    sentenceSelected.add(e.getSentence());
                }
            }
            if (sentenceSelected.size() == numberSelect || j == arr.size())
                break;
        }

        // print
        // for (int i : sentenceSelected) {
        // System.out.print(i + "\t");
        // }
        // System.out.println();
        return sentenceSelected;
    }

    public ArrayList<String> sumarize(ArrayList<String> sentences, ArrayList<Integer> selectedSentence) {
        Collections.sort(selectedSentence);

        ArrayList<String> arrAns = new ArrayList<>();
        int tmp = 0;
        for (int i : selectedSentence) {
            // System.out.print(i + "\t");
            arrAns.add(sentences.get(i-1));
//            if (tmp==0)
//                arrAns.add(sentences.get(i-1));// += sentences.get(i - 1);
//            else
//                 ans += "\n" + sentences.get(i - 1);
        }

        // System.out.println("\n" + ans);
        return arrAns;
    }

    public ArrayList<String> process(String text, ArrayList<String> arrStopWord, int sentenceCount) {
        // tach cau
        ArrayList<String> sentences = new ArrayList<>();
        String tc = "";
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '\n') {
                if (c == ' ' && tc.equals("")) {

                } else
                    tc += c;
            }
            if (c == '.' || c == '…' || c == '?' || c == '!') {

                if (i + 1 < text.length() && (text.charAt(i + 1) == ' ' || text.charAt(i + 1) == '\n')) {
                    if (tc.length() > 3)
                        sentences.add(tc);
                    tc = "";
                }else if (i+1 == text.length()) {
                    if (tc.length() > 3)
                        sentences.add(tc);
                    tc = "";
                }
            }
        }
        // print sentence
//		System.out.println("Sentences");
//		for (int i = 0; i < sentences.size(); i++) {
//			System.out.println(i + " - " + sentences.get(i));
//		}

        // String text = "This is a a sample. This is another another example example
        // example. sample.";//"Văn hoá của một tộc người nói chung và văn hoá Mường nói
        // riêng. Văn hoá ẩm thực, văn hoá trang phục một tộc người. Như vậy.";
        // String text = "Văn hóa của một tộc người nói chung và văn hóa Mường nói
        // riêng. Văn hóa ẩm thực, văn hóa trang phục một tộc người. This.";

        // System.out.println(text);

//        String tmp[] = tokenizer.tokenize(text);
        ArrayList<String> arrStr = (ArrayList<String>) tokenizer.tokenize(text);
        String s = "";
        for (String str : arrStr){
            s+= str + " ";
        }

//        String s = tmp[0];
        // String s = ReadFileText();
        // String s = "This is a sample. This is another example";
        s = s.toLowerCase();
        s = s.replaceAll(regex, "");

        String tachtu[] = s.split(" ");
        // System.out.println("So tu " + tachtu.length);
        ArrayList<String> token = new ArrayList<>();
        for (int i = 0; i < tachtu.length; i++) {
            if (!tachtu[i].equals("")) {
                token.add(tachtu[i]);
            }
        }

        Document document = process(sentences, token, arrStopWord);
        calTfidf(document);

        double graph[][] = contructGraph(document);

        int[] degree = degree(graph, document);

        // double g[][] = {{0,0,0},{0,0,1,1},{0,1,0,0},{0,1,0,0}};
        // int [] d = {0,2,1,1};
        // Document dc = new Document();
        // dc.setNumberSentence(3);

        // double pagerank[] = pagerank(g, d, dc);
        double pagerank[] = pagerank(graph, degree, document);

        // selectSentence(pagerank, g, dc, 0.35);
        double similarity = 0.3;
        // ti le nen
        double n = 0.3;
        sentenceCount = (int)(document.getNumberSentence()*n);
        ArrayList<Integer> selectedSentence = selectSentence(pagerank, graph, document, sentenceCount, similarity);

        ArrayList<String> ans = sumarize(sentences, selectedSentence);
        return ans;
    }

//
}

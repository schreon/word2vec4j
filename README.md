This is a prototypal implementation of Continuous Skip-gram Models (CSGM) using plain Java.
It has been developed in the scope of a student project at [Hochschule der Medien Stuttgart](https://www.hdm-stuttgart.de/). 
It yields competitive results when compared to gensim:


<table>
<caption>
<h3>word2vec4j vs. gensim regarding CSGM</h3>
</caption>
<thead>
<th>|v|=100</th><th>gensim(numpy)</th><th>gensim(cython)</th><th><i>word2vec4j</i></th><th>gensim(BLAS)</th>
</thead>
<tr><td>kwords/sec</td><td>0.16</td><td>180.11</td><td><b>205.11</b></td><td>309.87</td><tr>
</tr><td>docs/sec</td><td>0.11</td><td>138.75</td><td><b>145.19</b></td><td>238.28</td></tr>
</table>
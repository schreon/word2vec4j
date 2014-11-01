This is a prototypal implementation of Continuous Skip-gram Models (CSGM) using plain Java and the Fork/Join framework.
It has been developed in the scope of a student project at [Hochschule der Medien Stuttgart](https://www.hdm-stuttgart.de/). 
It yields competitive results when compared to gensim when applied to the first 50k articles of the german wikipedia:


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


This project is currently just a proof-of-concept.
Currently there are still paths to local files and folders specific to my machine.
The tests are crappy, Tt lacks documentation, and so on ...
It is really raw.
But it will be refined to a full-fledged library in the future. Stay tuned!
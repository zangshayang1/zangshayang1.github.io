---
layout: post
title:  Data Science Learning Notes
date:   2018-06-22 22:08:00 +0800
categories: Study-Notes
tag: dataScience
---

* content
{:toc}



## Chi Square Distribution

<img src="{{ '/styles/images/data-science-learning-notes/chi-square-distribution.png' }}" width="100%" />  


### Definition

$$ \chi^{2}(k) = \sum_{i=1}^{k} Z_{i} \;\;\; (Z_{i}  \tilde \; N(0, 1))$$

Sum of squares of k independent random variables that follow standard normal distribution.

### Use Case

#### Chi square test for variance

$$ \chi^{2} = \frac{(n-1)S^{2}}{\sigma_{0}^{2}} \;\;\; (H_{0}: \sigma^{2} = \sigma_{0}^2)$$
1. $(n-1)$ is degree of freedom.
2. $S$ is sample standard deviation.
3. $\sigma_{0}^{2}$ is population true/average variance.
4. $\sigma^{2}$ is sample true/average variance.

__Chi square test for variance is to determine if the sample variance has the same size as the population variance.__

Scenario:  
The customers of a hypothetical airline usually wait in a single large queue to drop off their luggage. The average variance of the waiting time in a single large queue is known and in this example comprises the population variance. A pilot study of 27 passengers used separate queues for each counter. Here, the average variance of the waiting times was measured for the separate queues, making up the sample variance in this case.  

[Full references about the above sample.](https://www.empirical-methods.hslu.ch/decisiontree/differences/variance/1-13chi-square-test-for-variance/)

__Notes:__
1. Chi square test is always one-sided test.

#### Chi square test for goodness of fit

$$ \chi^{2} = \sum \frac{(Observed - Expected)^2}{Expected} $$
$$ (H_{0}:\;Observed\;categorical\;variable\;X\;follow\;the\;distribution\;that\;the\;expected\;ones\;follow.)$$

__Chi square test for goodness of fit is to determine if a random sampling of categorical variable from a single population is consistent with expected distribution.__  

Scenario:  
A company printed baseball cards. It claimed that 30% of its cards were rookies; 60% were veterans but not All-Stars; and 10% were veteran All-Stars. We could gather a random sample of baseball cards and use a chi-square goodness of fit test to see whether our sample distribution differed significantly from the distribution claimed by the company.  

[Full references about the above sample.](https://stattrek.com/chi-square-test/goodness-of-fit.aspx?tutorial=ap)

#### Peek of central limit theorem from chi square test in binomial scenario

Scenario:  
Flip coins with head rate being p. N trials in total. m heads. (N-m) tails.

$$ \chi^{2} = \sum_{i}^{k} \frac{(observed_{i} - expected_{i})^{2}}{expected_{i}} $$
$$ \chi^{2} = \frac{(m - Np)^{2}}{Np} + \frac{(N - m - N(1-p))^{2}}{N(1-p)} $$
$$ \chi = \frac{m - Np}{\sqrt{Np(1-p)}} $$
$$ \chi \; \tilde \; N(Np, \;Np(1-p)) $$

# Linear Regression

$$ {\hat y} = \beta x + b $$

#### Assumption - Gaussian noise
Assume there is a linear regression model that perfectly explains the relation between features(X) and target(Y). Now in practice, noises occur on almost every datapoint. Meaning, even if a perfect linear model can be found, most of the datapoints in real life are not going to just hit the line perfectly. __It is claimed that these noises follow normal distribution, with the reason being: measurable noise is the sum of many random independent noises from different sources with similar magnitude (Central Limit Theorem).__  

#### LSE

__Least Squared Error__ is how you minimize sum of squared residuals:  
$$ \min_{\beta, b} ({\hat y} - y)^{2} $$
$$ \min_{\beta, b} (\beta x + b - y)^{2} $$

Find the partial derivatives and solve this in closed form:  
$$ \frac{\partial (\beta x + b - y)^{2}} {\partial \beta} = 0 $$
$$ \frac{\partial (\beta x + b - y)^{2}} {\partial b} = 0 $$

For a population X and true $\beta$:  
$$ \beta = \frac{Cov[X, Y]}{Var[X]} $$
$$ b = E[Y] - \beta E[X] $$

For a subset x and estimated ${\hat \beta}$:  
$$ {\hat \beta} = \frac{C_{xy}}{S_{x}^2} $$
$$ {\hat b} = {\bar y} - {\hat \beta} {\bar x} $$

And:  
$$ E[{\hat \beta}] = \beta $$
$$ Var[{\hat \beta}] = \frac{\sigma^2}{n s_{x}^2} $$
$$ E[{\hat b}] = b $$
$$ Var[{\hat b}] = \frac{\sigma^2}{n} (1 + \frac{{\bar x}^2}{s_{x}^2}) $$
$$ \sigma^2: variance \; of \; error \; term $$

Which means:  
$$ {\hat \beta} \tilde \; N(\beta, \; \frac{\sigma^2}{n s_{x}^2})$$
$$ {\hat b} \tilde \; N(b, \; \frac{\sigma^2}{n} (1 + \frac{{\bar x}^2}{s_{x}^2}) $$

Now if we provide:  
$$ H_{0}: \beta = 0 $$

We can use __t-test__ to exam the hypothesis:  
$$ t_{n-2} = \frac{{\hat \beta} - \mu}{\frac{s}{\sqrt{n}}} where \; \mu = E[{\hat \beta}], \; s = \sqrt{Var[{\hat \beta}]} $$

__Test conclusion:__ Only if the null hypothesis is rejected, can we say that including the feature x in the model can help explain y.

[wiki references](https://en.wikipedia.org/wiki/Student%27s_t-test#Slope_of_a_regression_line)

#### MLE
__Maximize Likelihood Estimator__ is how you figure out the parameters:  
$$ Likelihood(\beta_{0} = b_{0}, \beta_{1} = b_{1} | (x_{1}, y_{1}), (x_{2}, y_{2}), ..., (x_{n}, y_{n})) = Probability((x_{1}, y_{1}), (x_{2}, y_{2}), ..., (x_{n}, y_{n}) | \beta_{0} = b_{0}, \beta_{1} = b_{1}) $$
$$ Probability = \prod_{i}^{n} P(x = x_{i}, y = y_{i} | \beta_{0} = b_{0}, \beta_{1} = b_{1}) $$
Given that error term follows normal distribution centered around 0:    
$$ y - (\beta x + \beta_{0}) = \epsilon \tilde \; N(0, \sigma^{2}) $$
And:  
$$P(x = x_{i}, y = y_{i} | \beta_{0} = b_{0}, \beta_{1} = b_{1}) = P(\epsilon = \epsilon_{i}) $$
We have:  
$$ P(\epsilon = \epsilon_{i}) = \frac{1}{\sqrt{2\pi\sigma^2}} exp^{-\frac{(\epsilon_{i} - 0)^2}{2\sigma^2}} \; where \; \epsilon = y - (\beta x - \beta_{0})$$
If we take the logarithm over the above probability:  
$$ \log(Probability) = n \log{\frac{1}{\sqrt{2\pi\sigma^2}}} - \frac{1}{2\sigma^2} \sum_{i}^{n}(y_{i} - (bx + b_{0}))^2 $$
__So, maximizing the above likelihood is essentially minimizing the sum of squared residuals.__

#### Generalize LSE
$$ SSE(\beta) = ||Y - X \beta||$$
$$ {\hat \beta} = \underset{\beta}{\operatorname{argmin}} SSE(\beta) $$
$$ {\hat \beta} = (X^{T}X)^{-1} X^{T}Y $$
So:  
$$ {\hat Y} = X(X^{T}X)^{-1} X^{T} Y $$
$$ {\hat Y} = H Y $$
In other words:  
$$ {\hat y_{i}} = h_{i1}y_{1} + h_{i2}y_{2} + ... + h_{in}y_{n} \;\; i \in \{1, 2, ..., n\} $$
This is why we call it __y hat__.

[wiki references](https://en.wikipedia.org/wiki/Linear_least_squares_(mathematics))

#### Generalize MLE - mixed guassian distribution

TODO - EM algorithm
Please reference the learning materials in: https://www.youtube.com/watch?v=JNlEIEwe-Cg

#### Understand bias and variance
For a population X and a true model f(x), we have:  
$$ y = f(x) + \epsilon $$
For a subset X and an estimated model ${\hat f(x)}$, we have:  
$$ Expected\;squared\;error = E[(y - {\hat f(x)})^2] $$
$$ = E[({\hat f(x)} - f(x))^2 + E[({\hat f(x)} - E[{\hat f(x)}])^2] + \sigma_{\epsilon}^{2} $$
$$ = modelBias^2 + modelVariance + IrreducibleError^2 $$

#### R-square

__Coefficient of determination__, $R^{2}$, is the proportion of the variance in the dependent variable (y) that is predictable from the independent variable(s) (x).

3 related statistics:  
$$ SS_{totalVariance} = \sum_{i} (y_{i} - {\bar y})^2 $$
$$ SS_{regressionVariance} = \sum_{i} ({\hat y_{i}} - {\bar y})^2 $$
$$ SS_{residual} = \sum_{i} (y_{i} - {\hat y_{i}})^2 $$
$$ SS_{totalVariance} = SS_{regressionVariance} + SS_{residual} $$
$$ R^2 = \frac{SS_{regressionVariance}}{SS_{totalVariance}} = 1 - \frac{SS_{residual}}{SS_{totalVariance}} = 1 - FVU(fraction\;of\;variance\;unexplained)$$

__NOTE1: Understand irreducible error from geometric point of view__ if put into a high-dimension feature space, irreducible error or unexplained variance can be viewed as a vector that is the difference between target Y vector and yhat vector, which in addition is perpendicular to all the feature vectors, and thus not decomposable to any of them.

__NOTE2: Understand adjusted R square.__ As long as we keep adding independent features to the model, even when they are just noise, its R square value will keep increasing, which means R square is not sufficient to measure the goodness of fit of our model. Instead, we use [adjusted R square](https://en.wikipedia.org/wiki/Coefficient_of_determination#Adjusted_R2).

#### ANOVA and F-distribution
TODO

#### Regularization introduced by Multicollinearity

__Multicollinearity definition:__ one feature is highly linear to another. Or many to many.

__Multicollinearity consequence:__
1. Small shift on input data causes big change in model predications because collinear features contribute to the variance on the same direction.
2. If x1 is collinear with x2, the estimated coefficient for either of them will be imprecise to measure the independent effect of x1 or x2.

__Regularization to counteract multicollinearity:__
$$ min(Y - \beta X + \lambda |\beta|^{p}) $$
$$ \lambda: penalty \; factor $$
$$ lasso \; regularization \; when \; p = 1 $$
$$ ridge \; regularization \; when \; p = 2 $$

Lasso is sharper at feature selection, widely used in industry.

__Elastic net__ combines lasso and ridge:
$$ \lambda ((1 - \alpha)|\beta|^{2} + \alpha|\beta|)$$


# Logistic Regression

### Intro

__If linear regression is for model targeting continuous Y, logistic regression is for model targeting categorical Y.__

__Logistic function__ maps continuous X to probability intervals:
$$ logistic(x) = \frac{L}{1 + e^{-k (x - b)}} \;\; x \in (-\infty, +\infty) $$
$$ 0\le logistic(x) = \frac{L}{1 + e^{-k (x - b)}} \le 1 \; when\; L = 1 $$

__NOTES: Sigmoid function__ is a special case of logistic function:
$$ sigmoid(x) = \frac{1}{1 + e^{-x}} \;\; x \in (-\infty, +\infty) $$

Now in our case:  
$$ p(x) = \frac{1}{1 + e^{-\beta X}} $$
$$ e^{-\beta X} = \frac{1}{p(x)} - 1 $$
$$ \beta X = \ln{\frac{p(x)}{1 - p(x)}} $$
__From logistic regression to linear regression:__ the log of the odds ratio of logistic regression is equivalent to linear regression.

### Logistic loss function

Recall that logistic regression is targeting classification problem. Further recall bernoulli distribution:  
If heads up (k = 1) occurs at probability p and tails up (k = 0) at (1-p), we will have the likelihood of observing k being:  
$$ P(k;p) = p^{k} (1-p)^{1-k} $$

In case of logistic regression:
$$ P(y_{i} | x_{i}; \theta) = h_{\theta}(x_{i})^{y_{i}} (1-h_{\theta}(x_{i}))^{1-y_{i}} $$
$$ where\; h_{\theta} = \frac{1}{1 + e^{-\theta X}} $$

Assuming each observation $y_{i}$ is independent, then the probability of observing all of them given the model is:  
$$ P(Y | X; \theta) = \prod_{i=1}^{n}P(y_{i}|x_{i};\theta) $$

Then:  
$$ \ln{P(Y | X; \theta)} = \sum_{i=1}^{n} \ln{P(y_{i}|x_{i};\theta)} $$

And here comes of our logistic loss function:  
$$ \ln{P(Y | X; \theta)} = \sum_{i=1}^{n} [y_{i} \ln{h_{\theta}(x_{i})} + (1-y_{i}) (1-h_{\theta}(x_{i}))] $$

__After that, gradient descent comes into play.__

# Type I / Type II Error and False positive/negative

| | hypothesis is true | hypothesis is false |
|------------------|--------------------|----------------------
| accept hypothesis | correct decision | type II error |
| reject hypothesis | type I error | correct decision |

| | predict yes | predict no |
|------------------|--------------------|----------------------
| actual yes | correct | false negative |
| actual no | false positive | correct |


1. true positive rate (aka: __sensitivity, recall__) = true positive / (true positive + false negative)  

2. true negative rate (aka: __specificity__) = true negative / (true negative + false positive)  

3. false positive rate (aka: __1 - specificity__) = false positive / (false positive + true negative)

4. __precision = true positive / (true positive + false positive)__  

5. __accuracy = (true positive + true negative) / (predicated positive + predicted negative)__  

6. F-measure = ?  

# ROC curve following TPR/FPR

__ROC curve__ is plot of sensitivity against (1 - specificity) in a binary classification problem.

__Building ROC curve__:
```
Given model yhat = f(x), make predictions on test set.

Compare y_true and y_pred, accumulatively calculate (true positive rate, false positive rate).

Plot.
```

__NOTE:__ AUC is area under ROC curve, stand for the probability of  choosing positive predication over negative predication.

__Interpret ROC:__  
<img src="{{ '/styles/images/data-science-learning-notes/understand-roc-curve.png' }}" width="100%" />  

[ROC - wiki reference](https://en.wikipedia.org/wiki/Receiver_operating_characteristic)

# Bias and Variance
As model complexity increases, model bias on the training set decreases and model variances increases. The optimal model is achieved when the (bias + variance) is minimized. Before that point, the model is considered as underfitting. Beyond that point, the model is considered as overfitting, or say not generalize enough to be used in the real world.

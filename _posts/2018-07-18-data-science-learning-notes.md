---
layout: post
title:  Data Science Learning Notes
date:   2018-07-18 10:08:00 +0800
categories: Study-Notes
tag: dataScience
---

* content
{:toc}



## Chi Square Distribution

<img src="{{ '/styles/images/data-science-learning-notes/chi-square-distribution.png' }}" width="50%" />  


### Definition

$$ \chi^{2}(k) = \sum_{i=1}^{k} Z_{i} \;\;\; (Z_{i}  \tilde \; N(0, 1))$$  

Sum of squares of k independent random variables that follow standard normal distribution.

### Use Case

#### Chi square test for variance

$$ \chi^{2} = \frac{(n-1)S^{2}}{\sigma_{0}^{2}} \;\;\; (H_{0}: \sigma^{2} = \sigma_{0}^2)$$  

1. $$(n-1)$$ is degree of freedom.
2. $$S$$ is sample standard deviation.
3. $$\sigma_{0}^{2}$$ is population true/average variance.
4. $$\sigma^{2}$$ is sample true/average variance.

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


For a population X and true $$\beta$$:  

$$ \beta = \frac{Cov[X, Y]}{Var[X]} $$  

$$ b = E[Y] - \beta E[X] $$  

For a subset x and estimated $${\hat \beta}$$:  

$$ {\hat \beta} = \frac{C_{xy}}{S_{x}^2} $$  

$$ {\hat b} = {\bar y} - {\hat \beta} {\bar x} $$  

And:  

$$ E[{\hat \beta}] = \beta $$  

$$ Var[{\hat \beta}] = \frac{\sigma^2}{n s_{x}^2} $$  

$$ E[{\hat b}] = b $$  

$$ Var[{\hat b}] = \frac{\sigma^2}{n} (1 + \frac{\bar x^2}{s_{x}^2}) $$  

$$ \sigma^2: variance \; of \; error \; term $$  

Which means:  

$$ {\hat \beta} \tilde \; N(\beta, \; \frac{\sigma^2}{n s_{x}^2})$$  

$$ {\hat b} \tilde \; N(b, \; \frac{\sigma^2}{n} (1 + \frac{\bar x^2}{s_{x}^2}) $$  

Now if we provide:   

$$ H_{0}: \beta = 0 $$  

We can use __t-test__ to exam the hypothesis:  

$$ t_{n-2} = \frac{\hat \beta - \mu}{\frac{s}{\sqrt{n}}} where \; \mu = E[{\hat \beta}], \; s = \sqrt{Var[{\hat \beta}]} $$  

__Test conclusion:__ Only if the null hypothesis is rejected, can we say that including the feature x in the model can help explain y.

[wiki references](https://en.wikipedia.org/wiki/Student%27s_t-test#Slope_of_a_regression_line)

#### MLE
__Maximize Likelihood Estimator__ is how you figure out the parameters:  

$$ Likelihood(\beta_{0} = b_{0}, \beta_{1} = b_{1} | (x_{1}, y_{1}), (x_{2}, y_{2}), ..., (x_{n}, y_{n})) = Probability((x_{1}, y_{1}), (x_{2}, y_{2}), ..., (x_{n}, y_{n}) | \beta_{0} = b_{0}, \beta_{1} = b_{1}) $$  

Because each data point is independent:  

$$ Probability = \prod_{i}^{n} P(x = x_{i}, y = y_{i} | \beta_{0} = b_{0}, \beta_{1} = b_{1}) $$  

Given that error term follows normal distribution centered around 0:  

$$ y - (\beta x + \beta_{0}) = \epsilon \tilde \; N(0, \sigma^{2}) $$  

And:  

$$P(x = x_{i}, y = y_{i} | \beta_{0} = b_{0}, \beta_{1} = b_{1}) = P(\epsilon = \epsilon_{i}) $$  

We have:  

$$ P(\epsilon = \epsilon_{i}) = \frac{1}{\sqrt{2\pi\sigma^2}} exp^{-\frac{(\epsilon_{i} - 0)^2}{2\sigma^2}} \; where \; \epsilon_{i} = y_{i} - (\beta x_{i} - \beta_{0})$$  

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

For a subset X and an estimated model $${\hat f(x)}$$, we have:  

$$ Expected\;squared\;error = E[(y - {\hat f(x)})^2] $$  

$$ = E[({\hat f(x)} - f(x))^2 + E[({\hat f(x)} - E[{\hat f(x)}])^2] + \sigma_{\epsilon}^{2} $$  

$$ = modelBias^2 + modelVariance + IrreducibleError^2 $$  

#### R-square

__Coefficient of determination__, $$R^{2}$$, is the proportion of the variance in the dependent variable (y) that is predictable from the independent variable(s) (x).

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

Assuming each observation $$y_{i}$$ is independent, then the probability of observing all of them given the model is:  

$$ P(Y | X; \theta) = \prod_{i=1}^{n}P(y_{i}|x_{i};\theta) $$  

Then:  

$$ \ln{P(Y | X; \theta)} = \sum_{i=1}^{n} \ln{P(y_{i}|x_{i};\theta)} $$  

And here comes of our log likelihood function:  

$$ \ln{P(Y | X; \theta)} = \sum_{i=1}^{n} [y_{i} \ln{h_{\theta}(x_{i})} + (1-y_{i}) \ln (1-h_{\theta}(x_{i}))] \;\; where \; y \in \{0, 1\} $$  

Another formalism of logistic regression is:  

$$ P(Y = 1 | X; \theta) = h_{\theta}(X) $$  

$$ P(Y = -1 | X; \theta) = 1 - P(Y = 1 | X; \theta) = 1 - h_{\theta}(X) = h_{\theta}(-X) $$  

Unified to:  

$$ P(Y | X; \theta) = \frac{1}{1 + e^{-Y \theta X}} $$  

Essentially, they all comes down to a term:  

$$ 1 + e^{-Y \theta X} $$  

Which is the main body of __logistic loss function__:  

$$ \frac{1}{\ln2} ln{(1 + e^{-Y \theta X})}$$  

__To conclude, in a logistic regression problem, maximizing likelihood is equivalent to minimizing its loss.__

__After that, gradient descent comes into play.__

__NOTE:__ the loss function for linear regression, with predications being continuous, is sum of squared error; the loss function for logistic regression, with predications being categorical, is logistic loss, entropy, hinge loss, etc.

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
1. Given model yhat = f(x), make predictions on test set.

2. Compare y_true and y_pred, accumulatively calculate (true positive rate, false positive rate).

3. Plot.


__NOTE:__ AUC is area under ROC curve, stand for the probability of  choosing positive predication over negative predication.

__Interpret ROC:__  
<img src="{{ '/styles/images/data-science-learning-notes/understand-roc-curve.png' }}" width="100%" />  

[ROC - wiki reference](https://en.wikipedia.org/wiki/Receiver_operating_characteristic)

# Bias and Variance
As model complexity increases, model bias on the training set decreases and model variances increases. The optimal model is achieved when the (bias + variance) is minimized. Before that point, the model is considered as underfitting. Beyond that point, the model is considered as overfitting, or say not generalize enough to be used in the real world.

## Some picks on Machine Learning Models

### KNN
__KNN is subject to curse of dimension__, meaning when dimension of feature space is high, KNN becomes meaningless. Why? Quote from Prof. Pedro Domingos's paper at [here](https://homes.cs.washington.edu/~pedrod/papers/cacm12.pdf):  

_[O]ur intuitions, which come from a three-dimensional world, often do not apply in high-dimensional ones. In high dimensions, most of the mass of a multivariate Gaussian distribution is not near the mean, but in an increasingly distant “shell” around it; and most of the volume of a high-dimensional orange is in the skin, not the pulp. If a constant number of examples is distributed uniformly in a high-dimensional hypercube, beyond some dimensionality most examples are closer to a face of the hypercube than to their nearest neighbor. And if we approximate a hypersphere by inscribing it in a hypercube, in high dimensions almost all the volume of the hypercube is outside the hypersphere. This is bad news for machine learning, where shapes of one type are often approximated by shapes of another._  
_Another application, beyond machine learning, is nearest neighbor search: given an observation of interest, find its nearest neighbors (in the sense that these are the points with the smallest distance from the query point). But in high dimensions, a curious phenomenon arises: the ratio between the nearest and farthest points approaches 1, i.e. the points essentially become uniformly distant from each other. This phenomenon can be observed for wide variety of distance metrics, but it is more pronounced for the Euclidean metric than, say, Manhattan distance metric. The premise of nearest neighbor search is that "closer" points are more relevant than "farther" points, but if all points are essentially uniformly distant from each other, the distinction is meaningless._  


### Linear Regression

From KNN's perspective, if we use linear regression on a dataset, we are essentially cast features into a hyperplane where the linear predicator lies.

### Decision Tree

From a high level, we know that a decision tree model is built from calculating information gain at each feature and split at the point of highest gain. Essentially, this approach is calculating a local optimal instead of global optimal and it is better than linear regression in that each split de-correlates the respective feature from all other features. So in this sense, less noise due to feature dependencies.

### Unbalanced Data
1. If you have 100 data points and the unbalance ratio is 9: 1, apparently the number of data in minor group is too small for any training.
2. If you have 1000 data points and the same unbalance ratio, you can still train on 100 minor group. Which means, if you have more data, your model training can tolerate higher degree of data unbalance.
3. If you want an equal representation of the data, you can sub-sample major group to match minor group.


# Tree - Forest - XGB

### Tree

A tree model figures out the next optimal split in terms of which __feature__ to use and what __value__ to split. In a __regression__ tree, it is done by iteratively going through all features and every possible value in each feature to find out the minimum SSE. In a __classification__ tree, it is done by maximizing __information gain.__  

__Information Gain__ = prior entropy - posterior entropy.  

$$ Entropy(t) = - \sum_{j} p(j|t) \log{p(j|t)} $$  

Another option is Gini:  

$$ GINI(t) = 1 - \sum_{j} (p(j|t))^2 $$  

<img src="{{ '/styles/images/data-science-learning-notes/entropy-gini.png' }}" width="50%" />  

During training, if we allow the tree to grow without __regularization__, it will overfit the data for sure. To prune the tree, we add a penalty term to limit tree size (depth or number of leave nodes).

Another way to avoid overfitting is __bagging__.

### Bagging

Bagging is short for Bootstrap Aggregating. It is done by following steps:  
1. Bootstrap: create __m__ sub datasets out of original dataset by sampling with replacement.
2. Train m independent overfitted tree models on each sub dataset.
3. Aggregate these models to make predications on test dataset by either majority voting or averaging their results.

With the above bootstrap strategy, for each datapoint in the original dataset, the probability that it didn't get picked during the entire sampling process is: (sample n times)  
$$ p = (1 - \frac{1}{n})^{n} $$  

When n approach infinity, the above approach:  
$$ p = e^{-1} \approx 0.368 $$  

__Which means each bootstrapped dataset contains 63.2% of original dataset.__  

__NOTE:__ bagging helps complex model (deep tree with high variance) generalize well. For simple model (shallow tree with high bias), it doesn't help much.


__Problem__ with bagging: each "independent" model is still somewhat correlated with one another. After all, 63.2% means a lot of overlapped training data. To alleviate the correlation between trees, __random forest__ is developed.

### Forest

Based on __m-tree__ bagging strategy, forest can be built by randomly sampling __p__ features at each split, instead of using all features.  

For a classification problem with total Q features, optimal p is the square root of Q. In a regression problem, optimal p is Q/3.

__NOTE:__
1. For each tree grown on a bootstrapped dataset, the error rate on unused testing dataset (36.8%) is called "out-of-bag" error rate. The averaged out-of-bag error rate is very much similar to cross validation error rate.
2. One advantage about random forest is that the training of this model is easy to parallel.

### XGBoost

XGBoost is a popular library that we will talk later. Let's start from the most basic boosting - AdaBoosting.

#### AdaBoosting

Define a weak classifier to start:  

$$ {\hat y} = f_{\theta}^{1}(X) $$   

Minimize the SSE or logistic loss to find the optimal model:  

$$ L = (y - {\hat y})^{2} $$  

$$ \theta = \underset{\theta}{\operatorname{argmin}} L $$  

Compute error rate on validation dataset and use it to calculate expansion factor:  

$$ \alpha = \frac{1}{2} log(\frac{1 - e}{e}) $$  

Use it to increase the weights on mis-classified data points:  

$$ w_{2} = w_{1} e^{\alpha I(y_{i} <> {\hat y_{i}})} \; where \; I\; is\;identity\; matrix $$  

Now re-train the base model with weighted data points and achieve a new model that should gives lower bias by combining the previous classifier and the trained base model:  

$$ {\hat y} = f_{\theta}^{2}(X) = f_{\theta}^{1}(X) + f_{\theta}^{'}(X) $$  

Repeat the above steps for __m__ times (__m__ is the only hyperparameter in AdaBoosting so it is very easy to tune). Now aggregate __m__  classifiers weighted by each of their expansion factors to achieve our __AdaBoosting model__:  

$$ \sum_{i=1}^{m} \alpha_{i} f_{\theta}^{i}(X) $$  

__NOTES:__
1. Boosting is a additive model framework that usually starts on a tree.
2. More weights on a data point is equivalent to more number of such data points in normal weights.
3. Weak base model gives more space for boosting optimization. Strong ones leave less space to learn.


#### Gradient Boosting

Gradient Boosting differs from AdaBoosting in their ways to find the optimal base model. AdaBoosting does so by increasing weights over mis-classified data points. Gradient Boosting does so by computing the gradient of error residuals.

It usually starts with a constant predictor that equals the mean of data points to minimize the MSE:  

$$ {\hat y} = h_{0} = \theta_{0} x + \theta_{0c} $$  

$$ MSE = J(\theta_{0}) = \sum_{i=1}^{m}(y_{i} - {\hat y_{i}})^2 $$  
Gradient on error residuals:  

$$ \frac{\partial J(\theta_{0})}{\partial h_{0}} = y_{i} - {\hat y_{i}} $$  

$$ \nabla J(\theta_{0}) = Y - {\hat Y} $$  

Fit next model to the gradient:  

$$ h_{1} = \theta1  \frac{\partial J(\theta_{0})}{\partial h_{0}}  + \theta_{1c} $$  

Aggregate with previous model to __find local optimal this way__:  

$$ {\hat y}_{next} = h_{0} + \gamma_{i} h1$$  

Repeat the above for a number of times. This usually out-performs AdaBoosting.  

[wiki reference](https://en.wikipedia.org/wiki/Gradient_boosting)  
[kaggle reference](http://blog.kaggle.com/2017/01/23/a-kaggle-master-explains-gradient-boosting/)  

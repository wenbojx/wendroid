//
//  PlayerViewController.m
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#define kIdMin 1
#define kIdMax 1000
#import "PlayerViewController.h"
//#import <SDWebImage/UIImageView+WebCache.h>
#import "UIImageView+WebCache.h"
#import "SDWebImageDownloader.h"
#import "SDImageCache.h"

@interface PlayerViewController ()

@end

@implementation PlayerViewController

//@synthesize panoId;
@synthesize faceNum;
@synthesize hotspots;
@synthesize faceSL, faceSR, faceSF, faceSB, faceSD, faceSU;
//@synthesize faceDatas;
//@synthesize cubicPanorama;

-(void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    plView = (PLView *)self.view;
    plView.delegate = self;
    
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(panoPlayerNotificationHandler:) name:@"panoId" object:nil];
}

- (void)panoPlayerNotificationHandler:(NSNotification*)notification  
{  
    NSString *panoId = [[notification userInfo] objectForKey:@"panoId"];
    [self startPlayer:panoId];
} 

-(void)startPlayer:(NSString *)panoId{

    faceNum = 0;
    hotspots = [[NSMutableArray alloc] init];

    Boolean flag = false;
    
    
    if(panoId != nil){
        NSString *panoInfoUrl = [NSString stringWithFormat:@"http://beta1.yiluhao.com/ajax/m/pv/id/%@", panoId];
        NSLog(@"panoInfoUrl=%@", panoInfoUrl);
        NSString *responseData = [self getPanoInfoFromUrl:panoInfoUrl];
        if(responseData !=nil){
            NSDictionary *resultsDictionary = [responseData objectFromJSONString];
            NSArray *hotspotDct = [resultsDictionary objectForKey:@"hotspots"];
            //NSLog(@"res=%@", [ret objectForKey:@"panos"]);
            for(int i=0; i<hotspotDct.count; i++){
                NSDictionary  *tmp = [hotspotDct objectAtIndex:i];
                
                NSString *hotspotId = [tmp objectForKey:@"id"];
                NSString *link_scene_id = [tmp objectForKey:@"link_scene_id"];
                NSString *tilt = [tmp objectForKey:@"tilt"];
                NSString *pan = [tmp objectForKey:@"pan"];
                [self addHotspot:hotspotId linkSceneId:link_scene_id tilt:tilt pan:pan];
                //NSLog(@"id=%@", hotspotId);
                //NSLog(@"link_scene_id=%@", link_scene_id);
            }
            
            NSDictionary *pano = [resultsDictionary objectForKey:@"pano"];
            
            NSString *s_f = [pano objectForKey:@"s_f"];
            [self downLoadImage:s_f face:@"s_f"];
            NSString *s_r = [pano objectForKey:@"s_r"];
            [self downLoadImage:s_r face:@"s_r"];            
            NSString *s_b = [pano objectForKey:@"s_b"];
            [self downLoadImage:s_b face:@"s_b"];            
            NSString *s_l = [pano objectForKey:@"s_l"];
            [self downLoadImage:s_l face:@"s_l"];
            NSString *s_u = [pano objectForKey:@"s_u"];
            [self downLoadImage:s_u face:@"s_u"];
            NSString *s_d = [pano objectForKey:@"s_d"];
            [self downLoadImage:s_d face:@"s_d"];
            //NSLog(@"s_f=%@\n%@\n%@\n%@\n%@\n%@", s_f,s_r, s_b, s_l, s_u, s_d);
            flag = true;
        }
    }
    if(!flag){
        [self getWrong:@"加载数据出错"];
    }
    else {
        
    }
}

-(void)addHotspot:(NSString *)hotspotId linkSceneId:(NSString *)linkSceneId tilt:(NSString *)tilt pan:(NSString *)pan{
    
    NSMutableDictionary *hotspotBox = [[NSMutableDictionary alloc] init];
    [hotspotBox setValue:hotspotId forKey:@"hotspotId"];
    [hotspotBox setValue:linkSceneId forKey:@"linkSceneId"];
    [hotspotBox setValue:tilt forKey:@"tilt"];
    [hotspotBox setValue:pan forKey:@"pan"];
    
    [hotspots addObject:hotspotBox];
    //NSLog(@"hotspots%@", hotspots);
}
//下载图片
-(void)downLoadImage:(NSString *)url face:(NSString *)face{
    //SDWebImageManager *manager = [SDWebImageManager sharedManager];
    NSURL *faceUrl = [NSURL URLWithString:url];
    NSLog(@"url=%@\n", url);
    //UIImage *cachedImage = [manager imageWithURL:faceUrl]; // 将需要缓存的图片加载进来
    UIImage *cachedImage = [[SDImageCache sharedImageCache] imageFromKey:url];
    
    if (cachedImage) {
        NSLog(@"cached");
        //cachedImage
        [self initPlayer:face faceImage:cachedImage];
        //[cachedImage release], cachedImage=nil;
    } else {
        NSLog(@"nocached");
        // 如果Cache没有命中，则去下载指定网络位置的图片，并且给出一个委托方法
        [SDWebImageDownloader downloaderWithURL:faceUrl delegate:self userInfo:face];
        //[manager downloadWithURL:faceUrl delegate:self options:1];
    }
}

- (void)imageDownloader:(SDWebImageDownloader *)downloader 
     didFinishWithImage:(UIImage *)image {
    [downloader retain];
    if(image == nil){
        [self getWrong:@"加载素材出错"];
    }    
    [[SDImageCache sharedImageCache] storeImage:image imageData:downloader.imageData forKey:[downloader.url absoluteString] toDisk:true];
    NSLog(@"face=%@", downloader.userInfo);
    UIImage *faceImage = [[SDImageCache sharedImageCache] imageFromKey:[downloader.url absoluteString]];
    //[image release],image=nil;
    //[downloader release], downloader = nil;
    [self initPlayer:downloader.userInfo faceImage:faceImage];
}

-(void)initPlayer:(NSString *)face faceImage:(UIImage *)faceImage{
    //[self addFace:path face:face];
    faceNum++;

    if([face isEqualToString:@"s_f"]){
        faceSF = faceImage;
    }
    else if([face isEqualToString:@"s_l"]) {
        faceSL = faceImage;
    }
    else if([face isEqualToString:@"s_r"]) {
        faceSR = faceImage;
    }
    else if([face isEqualToString:@"s_b"]) {
        faceSB = faceImage;
    }
    else if([face isEqualToString:@"s_u"]) {
        faceSU = faceImage;
    }
    else if([face isEqualToString:@"s_d"]) {
        faceSD = faceImage;
    }

    if(faceNum > 5){
        
        [self displayPano];
    }
     
}

-(void)displayPano{
    
    NSObject<PLIPanorama> *panorama = nil;
    PLCubicPanorama *cubicPanorama = [PLCubicPanorama panorama];
    
    CGImageRef cgFaceSF = CGImageRetain(faceSF.CGImage);
    CGImageRef cgFaceSB = CGImageRetain(faceSB.CGImage);
    CGImageRef cgFaceSD = CGImageRetain(faceSD.CGImage);
    CGImageRef cgFaceSL = CGImageRetain(faceSL.CGImage);
	CGImageRef cgFaceSR = CGImageRetain(faceSR.CGImage);
    CGImageRef cgFaceSU = CGImageRetain(faceSU.CGImage);

    NSLog(@"wait time----");
    
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSF]] face:PLCubeFaceOrientationFront];
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSL]] face:PLCubeFaceOrientationLeft]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSR]] face:PLCubeFaceOrientationRight]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSB]] face:PLCubeFaceOrientationBack]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSU]] face:PLCubeFaceOrientationUp]; 
    [cubicPanorama setTexture:[PLTexture textureWithImage:[PLImage imageWithCGImage:cgFaceSD]] face:PLCubeFaceOrientationDown];
    panorama = cubicPanorama;
    for (int i=0; i<hotspots.count; i++) {
       
        NSMutableDictionary *hotspotDct = [hotspots objectAtIndex:i];
        PLTexture *hotspotTexture = [PLTexture textureWithImage:[PLImage imageWithPath:[[NSBundle mainBundle] pathForResource:@"hotspot" ofType:@"png"]]];
        NSString *hotspotId = [hotspotDct objectForKey:@"hotspotId"];
        NSString *tilt = [hotspotDct objectForKey:@"tilt"];
        NSString *pan = [hotspotDct objectForKey:@"pan"];
        
        PLHotspot *hotspot = [PLHotspot hotspotWithId:[hotspotId intValue] texture:hotspotTexture atv:[tilt floatValue] ath:[pan floatValue] width:0.08f height:0.08f];

        [panorama addHotspot:hotspot];
         
    }
    [plView setPanorama:panorama];
}

- (void) getWrong:(NSString*)str{
    NSString *msg = [NSString stringWithFormat:@"%@", str];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:nil message:msg delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil, nil];
    [alert show];
    [alert release];
}

-(NSString *)getPanoInfoFromUrl:(NSString *)url{
    NSURL *nsurl = [NSURL URLWithString:url];
    NSMutableURLRequest *theRequest = [[NSMutableURLRequest alloc] initWithURL:nsurl cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:60];
    //responseData = [[NSMutableData alloc] init];
    
    [theRequest addValue:@"gzip,deflate" forHTTPHeaderField:@"Accept-Encoding"];
    [theRequest addValue:@"*/*" forHTTPHeaderField:@"Accept"];
    [theRequest addValue:@"Mozilla/5.0 AppleWebKit/533.1 (KHTML, like Gecko)" forHTTPHeaderField:@"User-Agent"];
    //[theRequest addValue:@"http://www.cnbeta.com/" forHTTPHeaderField:@"Referer"];
    //[theRequest addValue:@"zh-cn" forHTTPHeaderField:@"Accept-Language"];
    
    NSString *responseData = [[NSString alloc] init];
    NSError* error = nil;
    
    NSData *requestData = [NSURLConnection sendSynchronousRequest:theRequest returningResponse:nil error:&error];
    if(error != nil){
        [self getWrong:[NSString stringWithFormat:@"获取服务器数据失败\n%@",[error localizedDescription]]];
        return nil;
    }
    responseData = [[[NSString alloc]initWithData:requestData encoding:NSUTF8StringEncoding]autorelease];
    [theRequest release];
    return responseData;
}


//Hotspot event
-(void)view:(UIView<PLIView> *)pView didClickHotspot:(PLHotspot *)hotspot screenPoint:(CGPoint)point scene3DPoint:(PLPosition)position
{
    /*UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Hotspot" message:[NSString stringWithFormat:@"You select the hotspot with ID %d", hotspot.identifier] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil, nil];*/
    NSString *clickHotspotId = [NSString stringWithFormat:@"%d", hotspot.identifier];
    NSString *panoId = nil;
    for (int i=0; i<hotspots.count; i++) {
        NSMutableDictionary *hotspotDct = [hotspots objectAtIndex:i];
        NSString *linkScence = [hotspotDct objectForKey:@"linkSceneId"];
        NSString *hotspotId = [hotspotDct objectForKey:@"hotspotId"];
        if([hotspotId isEqualToString:clickHotspotId]){
            panoId = linkScence;
        }
    }
    NSLog(@"panoID=%@", panoId);

    [self startPlayer:panoId];

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    //[cubicPanorama release], cubicPanorama=nil;
    [plView release], plView = nil;
}

-(void)dealloc
{
    [super dealloc];
}


-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

-(void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

-(BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return YES;
}

@end
